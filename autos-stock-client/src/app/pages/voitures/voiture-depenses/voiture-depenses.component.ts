import {Component, Input, OnInit} from '@angular/core';
import {BehaviorSubject, combineLatest, merge, Observable} from "rxjs";
import {ChartDataSets, ChartOptions} from "chart.js";
import {Label} from "ng2-charts";
import {DepenseDashboardVm} from "../../../models/DepenseDashboardVm";
import {filter, shareReplay, startWith, switchMap, tap} from "rxjs/operators";
import {PageVm} from "../../../models/PageVm";
import {DepenseDto} from "../../../models/DepenseDto";
import {DepenseService} from "../../../services/depense.service";
import {DepenseMonthlyTotalDto} from "../../../models/DepenseMonthlyTotalDto";
import {MatDialog} from "@angular/material/dialog";
import {
  VoitureDepenseDialogComponent
} from "../../features/voitures/voiture-depense-dialog/voiture-depense-dialog.component";
import {FournisseurService} from "../../../services/fournisseur.service";
import {Fournisseur} from "../../../models/fournisseur";

@Component({
  selector: 'app-voiture-depenses',
  templateUrl: './voiture-depenses.component.html',
  styleUrls: ['./voiture-depenses.component.scss']
})
export class VoitureDepensesComponent implements OnInit {

  @Input() voitureId!: any | null;
  @Input() isOwner!: boolean;
  @Input() refreshParent$!: Observable<void>;

  // UI
  showForm = false;
  onlyUnjustified = false;

  // paging
  private readonly page$ = new BehaviorSubject<number>(0);
  private readonly size$ = new BehaviorSubject<number>(10);

  // refresh
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  private readonly unjustified$ = new BehaviorSubject<boolean>(false);

  // chart period (12 derniers mois)
  depStart!: string;
  depEnd!: string;

  // chart (Angular 10 compatible: ng2-charts@2 + chart.js@2)
  barChartOptions: ChartOptions = {responsive: true};
  barChartLabels: Label[] = [];
  barChartType: any = 'bar';
  barChartLegend = true;
  barChartData: ChartDataSets[] = [{data: [], label: 'Dépenses mensuelles'}];

  depColumns: string[] = ['date', 'categorie', 'montant', 'fournisseur', 'justif', 'actions'];

  fournisseurs$ = new Map<number, Observable<Fournisseur>>();
  private trigger$!: Observable<void>;

  // dashboard (total + monthly)
  readonly dashboard$ = combineLatest([
    // this.refresh$
  ]).pipe();

  //page list
  readonly pageVm$ = combineLatest([
    this.page$,
    this.size$,
    this.unjustified$,
    // trigger$ sera défini en ngOnInit (voir plus bas)
    // on mettra un placeholder ici via une fonction
  ] as any).pipe(); // <-- on va remplacer juste après

  constructor(
    private dialog: MatDialog,
    private fSrv: FournisseurService,
    private depSrv: DepenseService
  ) {
  }

  ngOnInit(): void {
    // si voitureId arrive async via async pipe, ngOnInit peut s'exécuter avant
    // => on initialise la période et on attend la première vraie valeur
    const now = new Date();
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const start = new Date(now.getFullYear(), now.getMonth() - 11, 1);
    this.depStart = start.toISOString().slice(0, 10);
    this.depEnd = end.toISOString().slice(0, 10);

    this.trigger$ = merge(
      this.refreshParent$,
      this.refresh$
    ).pipe(startWith(void 0));

    // si voitureId déjà présent, on charge
    if (this.voitureId != null) {
      this.refresh();
    }

    (this as any).pageVm$ = combineLatest([
      this.page$,
      this.size$,
      this.unjustified$,
      this.trigger$
    ]).pipe(
      switchMap(([page, size, onlyUnjustified]) => {
        const id = this.voitureId!;
        return onlyUnjustified
          ? this.depSrv.listNonJustifiees(id, page, size)
          : this.depSrv.list(id, page, size);
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    (this as any).dashboard$ = combineLatest([
      this.trigger$
    ]).pipe(
      switchMap(() => this.depSrv.dashboard(this.requireVoitureId(), 0, 10, this.depStart, this.depEnd)),
      tap(d => this.applyChart(d.monthly || [])),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  // appelé automatiquement quand l'Input change (si tu veux être ultra safe)

  ngOnChanges(): void {
    if (this.voitureId != null) {
      this.refresh();
    }
  }

  toggleForm(id?): void {
    let data: any;
    if (id) {
      data = {idVoiture: this.voitureId, depenseId: id}
    } else {
      data = {idVoiture: this.voitureId}
    }
    const dialogRef = this.dialog.open(VoitureDepenseDialogComponent,
      {
        disableClose: true,
        width: '640px',
        data: data
      });
    dialogRef.afterClosed().subscribe(() => {
      console.log("dialog closed => refresh");
      this.refresh$.next()
      // this.refreshParent$.next()
    });
  }

  setOnlyUnjustified(v: boolean): void {
    this.onlyUnjustified = v;
    this.unjustified$.next(v);
    this.page$.next(0);
  }

  pageChange(pageIndex: number, pageSize: number): void {
    this.page$.next(pageIndex);
    this.size$.next(pageSize);
  }

  refresh(): void {
    this.refresh$.next(undefined);
    // this.refreshParent$.next(undefined);
  }

  private applyChart(rows: DepenseMonthlyTotalDto[]): void {
    this.barChartLabels = rows.map(r => `${r.year}-${String(r.month).padStart(2, '0')}`);
    this.barChartData = [{data: rows.map(r => Number(r.total)), label: 'Dépenses mensuelles'}];
  }

  private requireVoitureId(): number {
    if (this.voitureId == null) {
      // évite "undefined where a stream expected" + évite appels avant l'input
      throw new Error('voitureId est requis pour charger les dépenses.');
    }
    return Number(this.voitureId);
  }

  getFournisseur$(id: number): Observable<Fournisseur> {
    if(id) {
      if (!this.fournisseurs$.has(id)) {
        this.fournisseurs$.set(id, this.fSrv.getById(id));
      }
      return this.fournisseurs$.get(id)!;
    } else return null;
  }

  remove(id: number) {
    this.depSrv.delete(this.requireVoitureId(), id).subscribe(() => {
      this.refresh();
    });
  }

}
