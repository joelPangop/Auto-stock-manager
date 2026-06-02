import { Component, OnInit } from '@angular/core';
import {BehaviorSubject, combineLatest, Observable, Subject} from "rxjs";
import {PageVm} from "../../models/PageVm";
import {Paiement} from "../../models/paiement";
import {debounceTime, map, shareReplay, switchMap, takeUntil} from "rxjs/operators";
import {distinctUntilChanged} from "rxjs/internal/operators";
import {PaiementService} from "../../services/paiement.service";
import {MatDialog} from "@angular/material/dialog";
import {AuthService} from "../../services/auth.service";
import {Sort} from "@angular/material/sort";
import {PageEvent} from "@angular/material/paginator";
import {PaiementViewDialogComponent} from "../features/paiement-view-dialog/paiement-view-dialog.component";

@Component({
  selector: 'app-paiements',
  templateUrl: './paiements.component.html',
  styleUrls: ['./paiements.component.scss']
})
export class PaiementsComponent implements OnInit {

  displayedColumns: string[] = ['datePaiement', 'voitureLabel', 'montant', 'methode', 'actions'];

  onlyMine = false;
  searchText = '';
  isAdmin = false;

  private readonly page$ = new BehaviorSubject<number>(0);
  private readonly size$ = new BehaviorSubject<number>(10);
  private readonly sort$ = new BehaviorSubject<string>('datePaiement,desc');
  private readonly onlyMine$ = new BehaviorSubject<boolean>(false);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  private readonly search$ = new BehaviorSubject<string>('');

  private readonly destroy$ = new Subject<void>();

  readonly pageVm$: Observable<PageVm<Paiement>> = combineLatest([
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
      this.paiementSrv.getPage(page, size, sort, onlyMine).pipe(
        map(vm => this.applySearch(vm, search))
      )
    ),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  constructor(
    private readonly paiementSrv: PaiementService,
    private readonly dialog: MatDialog,
    private readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

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
    const active = sort.active || 'datePaiement';
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

  openView(id: number): void {
    this.dialog.open(PaiementViewDialogComponent, {
      width: '760px',
      disableClose: true,
      data: { paiementId: id }
    });
  }

  openEdit(id: number): void {
    // réservé admin : ouvre ton dialog edit si tu l'as
    // this.dialog.open(PaiementEditDialogComponent, {...})
  }

  confirmDelete(p: Paiement): void {
    // if (!this.isAdmin) return;
    //
    // const ref = this.dialog.open(ConfirmDialogComponent, {
    //   width: '440px',
    //   disableClose: true,
    //   data: {
    //     title: 'Supprimer le paiement',
    //     message: `Voulez-vous supprimer ce paiement (#${p.id}) ?`,
    //     confirmText: 'Supprimer',
    //     cancelText: 'Annuler'
    //   }
    // });
    //
    // ref.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((ok: boolean) => {
    //   if (!ok) return;
    //
    //   this.paiementSrv.delete(p.id).subscribe({
    //     next: () => this.reload(),
    //     error: err => console.error(err)
    //   });
    // });
  }

  private applySearch(vm: PageVm<Paiement>, search: string): PageVm<Paiement> {
    if (!search) return vm;

    const q = search.toLowerCase();
    const items = (vm.items ?? []).filter(p => {
      const voiture = (p.voitureLabel || '').toLowerCase();
      const methode = (p.methode || '').toLowerCase();
      const ref = (p.reference || '').toLowerCase();
      const montant = String(p.montant ?? '').toLowerCase();
      const date = String(p.datePaiement ?? '').toLowerCase();
      const vente = String(p.venteId ?? '').toLowerCase();
      return voiture.includes(q) || methode.includes(q) || ref.includes(q)
        || montant.includes(q) || date.includes(q) || vente.includes(q);
    });

    return { ...vm, items };
  }
}
