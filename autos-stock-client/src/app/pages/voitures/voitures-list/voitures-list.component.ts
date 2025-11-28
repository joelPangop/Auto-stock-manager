import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import {VoitureService} from "../../../services/voiture.service";
import {StatutVoiture} from "../../../models/enums/StatutVoiture";
import {VoitureListDto} from "../../../models/VoitureListDto";
import {Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {
  VoitureCreateDialogComponent
} from "../../features/voitures/voiture-create-dialog/voiture-create-dialog.component";

@Component({
  selector: 'app-voitures-list',
  templateUrl: './voitures-list.component.html',
  styleUrls: ['./voitures-list.component.scss']
})
export class VoituresListComponent implements OnInit, AfterViewInit, OnDestroy {
  displayedColumns = ['id', 'marque', 'modele', 'annee', 'couleur', 'vin', 'prixVente', 'statut', 'actions'];
  data = new MatTableDataSource<VoitureListDto>([]);
  loading = false;

  backendFiltering = false; // on garde le mode backend

  // filtres pilotés par Reactive Forms
  form!: FormGroup;

  total = 0;
  pageSize = 10;
  pageIndex = 0;

  private destroy$ = new Subject<void>();
  private subAll?: Subscription;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private voitureService: VoitureService, private fb: FormBuilder, private router: Router, private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      marque: new FormControl(''),
      statut: new FormControl(null)
    });

    // 🔁 Abonnement unique et debounced aux changements des filtres
    this.form.valueChanges
      .pipe(debounceTime(250), takeUntil(this.destroy$))
      .subscribe(() => this.reload());

    // 1er chargement
    this.reload();
  }

  ngAfterViewInit(): void {
    if (!this.backendFiltering) {
      this.data.paginator = this.paginator;
      this.data.sort = this.sort;
    }
  }

  reload(): void {
    this.loading = true;

    const marque = this.form.controls.marque.value?.trim() || undefined;
    const statut = this.form.controls.statut.value || undefined;

    if (this.backendFiltering) {
      this.voitureService.list(marque, statut).subscribe({
        next: list => {
          this.data.data = list;
          this.total = list.length;
          this.loading = false;
        },
        error: _ => this.loading = false
      });
    } else {
      this.subAll?.unsubscribe();
      this.subAll = this.voitureService.list().subscribe({
        next: all => {
          const m = (marque ?? '').toLowerCase();
          const filtered = all.filter(v => {
            const okM = m ? (v.marque?.toLowerCase().includes(m) ?? false) : true;
            const okS = statut ? v.statut === statut : true;
            return okM && okS;
          });
          this.data.data = filtered;
          this.total = filtered.length;
          this.data.paginator = this.paginator;
          this.data.sort = this.sort;
          this.loading = false;
        },
        error: _ => this.loading = false
      });
    }
  }

  clearFilters() {
    this.form.reset({marque: '', statut: null}, {emitEvent: true});
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.subAll?.unsubscribe();
  }

  goToDetail(id: number) {
    this.router.navigate(['/voitures', id]);
  }

  delete(v: VoitureListDto) {
    if (!confirm(`Supprimer la voiture #${v.id} (${v.marque} ${v.modele}) ?`)) {
      return;
    }
    this.voitureService.delete(v.id).subscribe({
      next: () => this.reload(),
      error: () => alert('Suppression impossible pour le moment.')
    });
  }

  nouvelleVoiture() {
    const ref = this.dialog.open(VoitureCreateDialogComponent, {width: '760px'});
    ref.afterClosed().subscribe(created => {
      if (created) {
        // si tu es en pagination backend, relance getPage
        // sinon, relance getAll
        this.reload(); // ta méthode existante
      }
    });
  }
}
