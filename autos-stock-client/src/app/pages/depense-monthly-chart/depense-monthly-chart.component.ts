import {Component, Input, OnInit} from '@angular/core';
import {DepenseMonthlyTotalDto} from "../../models/DepenseMonthlyTotalDto";
import {ChartDataSets, ChartOptions} from "chart.js";
import {Label} from "ng2-charts";
import {DepenseService} from "../../services/depense.service";

@Component({
  selector: 'app-depense-monthly-chart',
  templateUrl: './depense-monthly-chart.component.html',
  styleUrls: ['./depense-monthly-chart.component.scss']
})
export class DepenseMonthlyChartComponent implements OnInit {

  @Input() voitureId!: number;

  public barChartOptions: ChartOptions = {
    responsive: true
  };

  public barChartLabels: Label[] = [];
  public barChartType: any = 'bar';
  public barChartLegend = true;

  public barChartData: ChartDataSets[] = [
    { data: [], label: 'Dépenses mensuelles' }
  ];

  start!: string;
  end!: string;

  constructor(private depSrv: DepenseService) {}

  ngOnInit(): void {
    const now = new Date();
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const start = new Date(now.getFullYear(), now.getMonth() - 11, 1);

    this.start = start.toISOString().slice(0, 10);
    this.end = end.toISOString().slice(0, 10);

    this.load();
  }

  load(): void {
    this.depSrv.monthly(this.voitureId, this.start, this.end).subscribe({
      next: rows => this.apply(rows),
      error: err => console.error(err)
    });
  }

  private apply(rows: DepenseMonthlyTotalDto[]): void {
    this.barChartLabels = rows.map(r => `${r.year}-${String(r.month).padStart(2, '0')}`);
    this.barChartData = [
      { data: rows.map(r => Number(r.total)), label: 'Dépenses mensuelles' }
    ];
  }
}
