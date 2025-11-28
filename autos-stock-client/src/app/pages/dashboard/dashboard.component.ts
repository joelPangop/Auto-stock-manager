import {ChangeDetectionStrategy, Component} from "@angular/core";
import {Router} from '@angular/router';
import {shareReplay} from 'rxjs/operators';
import {StatsService} from "../../services/stats.service";

type Stats = { voitures: number; ventes: number; clients: number };

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  voituresCount$ = this.statsSrv.voituresCount().pipe(shareReplay(1));
  ventesCount$   = this.statsSrv.ventesCount().pipe(shareReplay(1));
  clientsCount$  = this.statsSrv.clientsCount().pipe(shareReplay(1));

  constructor(private statsSrv: StatsService, private router: Router) { }

  go(url: string) {
    this.router.navigateByUrl(url);
  }
}
