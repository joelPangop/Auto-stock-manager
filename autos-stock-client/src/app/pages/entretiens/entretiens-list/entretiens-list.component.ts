import { Component, OnInit } from '@angular/core';
import {distinctUntilChanged, switchMap} from "rxjs/internal/operators";
import {debounceTime, map, shareReplay} from "rxjs/operators";
import {BehaviorSubject, combineLatest, Observable, Subject} from "rxjs";
import {EntretienService} from "../../../services/entretien.service";
import {MatDialog} from "@angular/material/dialog";
import {Sort} from "@angular/material/sort";
import {PageEvent} from "@angular/material/paginator";
import {PageVm} from "../../../models/PageVm";
import {Entretien} from "../../../models/entretien";
import {AuthService} from "../../../services/auth.service";
import {
  EntretienViewDialogComponent
} from "../../features/entretien/entretien-view-dialog/entretien-view-dialog.component";

@Component({
  selector: 'app-entretiens-list',
  templateUrl: './entretiens-list.component.html',
  styleUrls: ['./entretiens-list.component.scss']
})
export class EntretiensListComponent implements OnInit {
  displayedColumns: string[] = ['dateEntretien', 'voiture', 'typeLabel', 'commentaire', 'cout', 'actions'];
  onlyMine = false;
  searchText = '';
  isAdmin = false;

  // ====== state streams ======
  private readonly page$ = new BehaviorSubject<number>(0);
  private readonly size$ = new BehaviorSubject<number>(10);
  private readonly sort$ = new BehaviorSubject<string>('dateEntretien,desc');
  private readonly onlyMine$ = new BehaviorSubject<boolean>(false);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  private readonly search$ = new BehaviorSubject<string>('');

  private readonly destroy$ = new Subject<void>();

  // ====== VM ======
  readonly pageVm$: Observable<PageVm<Entretien>> = combineLatest([
    this.page$,
    this.size$,
    this.sort$,
    this.onlyMine$,
    this.search$.pipe(
      map(s => (s ?? '').trim()),
      debounceTime(250),
      distinctUntilChanged()
    ),
    this.refresh$
  ]).pipe(
    switchMap(([page, size, sort, onlyMine, search, _refresh]) =>
      this.entretienSrv.getPage(page, size, sort, onlyMine).pipe(
        map(vm => this.applySearch(vm, search))
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );


  constructor(
    private readonly entretienSrv: EntretienService,
    private auth: AuthService,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    // Rien d’obligatoire ici. Streams déjà prêts.
    this.isAdmin = this.auth.isAdmin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ====== handlers appelés par le HTML ======
  toggleOnlyMine(checked: boolean): void {
    this.onlyMine = checked;
    this.onlyMine$.next(checked);
    this.page$.next(0); // retour page 0
  }

  onSearch(text: string): void {
    this.searchText = text ?? '';
    this.page$.next(0);
    this.search$.next(this.searchText);
  }

  onSort(sort: Sort): void {
    // sort.active = colonne, sort.direction = 'asc' | 'desc' | ''
    const dir = sort.direction || 'desc';
    const active = sort.active || 'dateEntretien';
    // ⚠️ Assure-toi que "active" correspond à un champ triable côté backend
    this.sort$.next(`${active},${dir}`);
    this.page$.next(0);
  }

  onPage(ev: PageEvent): void {
    this.page$.next(ev.pageIndex);
    this.size$.next(ev.pageSize);
  }

  reload(): void {
    this.refresh$.next(undefined);
  }

  openView(entretien: Entretien) {
    this.dialog.open(EntretienViewDialogComponent, {
      width: '760px',
      disableClose: true,
      data: { entretien: entretien }
    });
  }


  // openCreate(): void {
  //   const ref = this.dialog.open(VoitureEntretienDialogComponent, {
  //     width: '720px',
  //     disableClose: true,
  //     data: {mode: 'create'}
  //   });
  //
  //   ref.afterClosed()
  //     .pipe(takeUntil(this.destroy$))
  //     .subscribe((changed: boolean) => {
  //       if (changed) this.reload();
  //     });
  // }
  //
  // openEdit(id: number): void {
  //   const ref = this.dialog.open(VoitureEntretienDialogComponent, {
  //     width: '720px',
  //     disableClose: true,
  //     data: {mode: 'edit', entretienId: id}
  //   });
  //
  //   ref.afterClosed()
  //     .pipe(takeUntil(this.destroy$))
  //     .subscribe((changed: boolean) => {
  //       if (changed) this.reload();
  //     });
  // }
  //
  // confirmDelete(e: Entretien): void {
  //   const ref = this.dialog.open(ConfirmDialogComponent, {
  //     width: '440px',
  //     disableClose: true,
  //     data: {
  //       title: 'Supprimer l’entretien',
  //       message: `Voulez-vous vraiment supprimer cet entretien du ${this.formatDate(e.dateEntretien)} ?`,
  //       confirmText: 'Supprimer',
  //       cancelText: 'Annuler'
  //     }
  //   });
  //
  //   ref.afterClosed()
  //     .pipe(takeUntil(this.destroy$))
  //     .subscribe((ok: boolean) => {
  //       if (!ok) return;
  //
  //       // ⚠️ adapte selon ton API: delete(id) ou delete(idVoiture, id)
  //       this.entretienSrv.delete(e.id).subscribe({
  //         next: () => this.reload(),
  //         error: err => console.error(err)
  //       });
  //     });
  // }

  // ====== helpers ======
  private applySearch(vm: PageVm<Entretien>, search: string): PageVm<Entretien> {
    if (!search) return vm;

    const q = search.toLowerCase();

    const filtered = (vm.items ?? []).filter(e => {
      const type = (e.typeLabel || e.type || '').toLowerCase();
      const com = (e.commentaire || '').toLowerCase();
      const cout = (e.cout ?? '').toString().toLowerCase();
      const date = (e.dateEntretien ?? '').toString().toLowerCase();
      return type.includes(q) || com.includes(q) || cout.includes(q) || date.includes(q);
    });

    // ⚠️ Ici on filtre la page courante seulement.
    // Si tu veux un vrai search global, ajoute un param "q" côté backend.
    return {
      ...vm,
      items: filtered,
      totalElements: filtered.length,
      totalPages: 1,
      page: 0
    };
  }

  private formatDate(d: any): string {
    try {
      const dt = new Date(d);
      if (isNaN(dt.getTime())) return String(d);
      return dt.toISOString().slice(0, 10);
    } catch {
      return String(d);
    }
  }
}
