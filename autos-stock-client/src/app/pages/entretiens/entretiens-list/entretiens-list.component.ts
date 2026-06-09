import { Component, OnInit } from '@angular/core';
import {distinctUntilChanged, switchMap} from "rxjs/internal/operators";
import {debounceTime, map, shareReplay, takeUntil} from "rxjs/operators";
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
import {
  EntretienEditDialogComponent
} from "../../features/entretien/entretien-edit-dialog/entretien-edit-dialog.component";

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
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ====== handlers ======
  toggleOnlyMine(checked: boolean): void {
    this.onlyMine = checked;
    this.onlyMine$.next(checked);
    this.page$.next(0);
  }

  onSearch(text: string): void {
    this.searchText = text ?? '';
    this.page$.next(0);
    this.search$.next(this.searchText);
  }

  onSort(sort: Sort): void {
    const dir = sort.direction || 'desc';
    const active = sort.active || 'dateEntretien';
    this.sort$.next(active + ',' + dir);
    this.page$.next(0);
  }

  onPage(ev: PageEvent): void {
    this.page$.next(ev.pageIndex);
    this.size$.next(ev.pageSize);
  }

  reload(): void {
    this.refresh$.next(undefined);
  }

  openView(entretien: Entretien): void {
    this.dialog.open(EntretienViewDialogComponent, {
      width: '760px',
      disableClose: true,
      data: { entretien: entretien }
    });
  }

  openEdit(entretien: Entretien): void {
    const ref = this.dialog.open(EntretienEditDialogComponent, {
      width: '720px',
      disableClose: true,
      data: { idVoiture: entretien.idVoiture, entretien: entretien }
    });

    ref.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result) { this.reload(); }
      });
  }

  confirmDelete(e: Entretien): void {
    const msg = 'Voulez-vous vraiment supprimer cet entretien du ' + this.formatDate(e.dateEntretien) + ' ?';
    if (!window.confirm(msg)) { return; }

    this.entretienSrv.delete(e.id!).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => this.reload(),
      error: err => console.error(err)
    });
  }

  // ====== helpers ======
  private applySearch(vm: PageVm<Entretien>, search: string): PageVm<Entretien> {
    if (!search) { return vm; }

    const q = search.toLowerCase();
    const filtered = (vm.items ?? []).filter(e => {
      const type = (e.typeLabel || e.type || '').toLowerCase();
      const com = (e.commentaire || '').toLowerCase();
      const cout = (e.cout ?? '').toString().toLowerCase();
      const date = (e.dateEntretien ?? '').toString().toLowerCase();
      return type.includes(q) || com.includes(q) || cout.includes(q) || date.includes(q);
    });

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
      if (isNaN(dt.getTime())) { return String(d); }
      return dt.toISOString().slice(0, 10);
    } catch {
      return String(d);
    }
  }
}
