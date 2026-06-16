import {Component, OnInit} from '@angular/core';
import {DocumentService} from '../../services/document.service';
import {DocumentRichDto} from '../../models/DocumentRichDto';
import {Router} from '@angular/router';

export interface FolderVm {
  type: string;
  typeLabel: string;
  icon: string;
  documents: DocumentRichDto[];
  open: boolean;
  count: number;
}

const TYPE_ICONS: Record<string, string> = {
  FACTURE: 'receipt_long',
  CARFAX: 'history',
  PHOTO: 'photo_library',
  IMMATRICULATION: 'credit_card',
  INSPECTION: 'fact_check',
  ASSURANCE: 'security',
  AUTRE: 'folder'
};

@Component({
  selector: 'app-documents',
  templateUrl: './documents.component.html',
  styleUrls: ['./documents.component.scss']
})
export class DocumentsComponent implements OnInit {

  folders: FolderVm[] = [];
  loading = false;
  error = '';
  searchText = '';
  allDocs: DocumentRichDto[] = [];

  displayedColumns: string[] = [
    'nomFichier', 'voiture', 'vendeur', 'client', 'montant', 'dateUpload', 'description', 'actions'
  ];

  constructor(private docSrv: DocumentService, private router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error = '';
    this.docSrv.listAll().subscribe({
      next: (docs) => {
        this.allDocs = docs || [];
        this.buildFolders(this.allDocs);
      },
      error: (err) => {
        this.error = 'Erreur HTTP ' + (err?.status || '') + ': ' + (err?.message || 'inconnue');
      }
    });
  }

  private buildFolders(docs: DocumentRichDto[]): void {
    const map = new Map<string, DocumentRichDto[]>();
    docs.forEach(d => {
      const key = d.type || 'AUTRE';
      if (!map.has(key)) { map.set(key, []); }
      map.get(key)!.push(d);
    });
    const order = ['FACTURE', 'CARFAX', 'IMMATRICULATION', 'INSPECTION', 'ASSURANCE', 'PHOTO', 'AUTRE'];
    const sorted = Array.from(map.entries()).sort(([a], [b]) => {
      return (order.indexOf(a) === -1 ? 99 : order.indexOf(a)) -
             (order.indexOf(b) === -1 ? 99 : order.indexOf(b));
    });
    this.folders = sorted.map(([type, documents]) => ({
      type,
      typeLabel: documents[0]?.typeLabel || type,
      icon: TYPE_ICONS[type] || 'folder',
      documents,
      open: false,
      count: documents.length
    }));
  }

  toggleFolder(folder: FolderVm): void { folder.open = !folder.open; }

  onSearch(text: string): void {
    this.searchText = text;
    if (!text.trim()) { this.buildFolders(this.allDocs); return; }
    const q = text.toLowerCase();
    const filtered = this.allDocs.filter(d =>
      (d.nomFichier || '').toLowerCase().includes(q) ||
      (d.voitureLabel || '').toLowerCase().includes(q) ||
      (d.vendeurNom || '').toLowerCase().includes(q) ||
      (d.clientNom || '').toLowerCase().includes(q) ||
      (d.typeLabel || '').toLowerCase().includes(q) ||
      (d.description || '').toLowerCase().includes(q)
    );
    this.buildFolders(filtered);
    this.folders.forEach(f => { f.open = true; });
  }

  goToVoiture(voitureId: number | undefined): void {
    if (voitureId) { this.router.navigate(['/voitures', voitureId]); }
  }

  download(doc: DocumentRichDto): void {
    this.docSrv.download(doc.id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = window.document.createElement('a');
      a.href = url;
      a.download = doc.nomFichier || ('document_' + doc.id);
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  delete(doc: DocumentRichDto, folder: FolderVm): void {
    if (!window.confirm('Supprimer ce document ?')) { return; }
    this.docSrv.delete(doc.id).subscribe({
      next: () => {
        folder.documents = folder.documents.filter(d => d.id !== doc.id);
        folder.count = folder.documents.length;
        this.allDocs = this.allDocs.filter(d => d.id !== doc.id);
        if (folder.count === 0) { this.folders = this.folders.filter(f => f.type !== folder.type); }
      }
    });
  }

  isImage(doc: DocumentRichDto): boolean {
    return !doc.nomFichier ? false : /\.(jpg|jpeg|png|gif|webp)$/i.test(doc.nomFichier);
  }

  isPdf(doc: DocumentRichDto): boolean {
    return !doc.nomFichier ? false : /\.pdf$/i.test(doc.nomFichier);
  }

  totalMontant(docs: DocumentRichDto[]): number {
    return docs.reduce((sum, d) => sum + (d.montant || 0), 0);
  }
}
