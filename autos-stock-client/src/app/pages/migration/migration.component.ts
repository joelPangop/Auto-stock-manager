import {Component} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BackupService} from '../../services/backup.service';
import {HttpResponse} from '@angular/common/http';

@Component({
  selector: 'app-migration',
  templateUrl: './migration.component.html',
  styleUrls: ['./migration.component.scss']
})
export class MigrationComponent {

  includeUploads = true;
  includeS3 = true;

  running = false;
  lastFile: string | null = null;
  lastSize: string | null = null;
  errorMsg: string | null = null;

  constructor(private api: BackupService, private snack: MatSnackBar) {}

  export() {
    if (this.running) return;
    this.running = true;
    this.errorMsg = null;

    this.api.export(this.includeUploads, this.includeS3).subscribe({
      next: (res) => {
        this.running = false;
        this.save(res);
      },
      error: (err) => {
        this.running = false;
        this.errorMsg = this.describe(err);
        this.snack.open(this.errorMsg, 'Fermer', {duration: 8000});
      }
    });
  }

  private save(res: HttpResponse<Blob>) {
    const blob = res.body;
    if (!blob || blob.size === 0) {
      this.errorMsg = 'Le serveur a renvoyé une archive vide.';
      this.snack.open(this.errorMsg, 'Fermer', {duration: 8000});
      return;
    }

    const name = this.filenameFrom(res) ?? 'autostock-export.zip';

    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.click();
    URL.revokeObjectURL(url);

    this.lastFile = name;
    this.lastSize = this.humanSize(blob.size);
    this.snack.open('Export terminé ✔', 'OK', {duration: 4000});
  }

  private filenameFrom(res: HttpResponse<Blob>): string | null {
    const header = res.headers.get('Content-Disposition');
    const match = header?.match(/filename="?([^"]+)"?/);
    return match ? match[1] : null;
  }

  /**
   * Le corps d'erreur est un Blob (responseType: 'blob'), donc le message du
   * serveur n'est pas lisible directement : on se rabat sur le code HTTP.
   */
  private describe(err: any): string {
    switch (err?.status) {
      case 429:
        return 'Un export a déjà eu lieu il y a moins de 5 minutes. Réessayez plus tard.';
      case 401:
        return 'Session expirée. Reconnectez-vous.';
      case 403:
        return 'Réservé au super administrateur.';
      case 0:
        return 'Connexion interrompue. Sur une grosse base, l\'export peut dépasser le délai du proxy.';
      default:
        return `Échec de l'export (code ${err?.status ?? 'inconnu'}).`;
    }
  }

  private humanSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} Mo`;
    return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} Go`;
  }
}
