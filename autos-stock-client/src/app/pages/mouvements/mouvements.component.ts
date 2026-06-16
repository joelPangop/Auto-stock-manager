import {Component, OnInit} from '@angular/core';
import {MouvementService} from '../../services/mouvement.service';
import {Mouvement} from '../../models/mouvement';
import {Router} from '@angular/router';

export interface TimelineGroup {
  label: string;
  mouvements: Mouvement[];
}

const TYPE_CONFIG: Record<string, { color: string; icon: string; bg: string }> = {
  IN:      { color: '#2e7d32', icon: 'add_circle',    bg: '#e8f5e9' },
  OUT:     { color: '#e65100', icon: 'remove_circle', bg: '#fff3e0' },
  SALE:    { color: '#1565c0', icon: 'sell',          bg: '#e3f2fd' },
  RETURN:  { color: '#f9a825', icon: 'undo',          bg: '#fffde7' },
  RESERVE: { color: '#6a1b9a', icon: 'bookmark',      bg: '#f3e5f5' }
};
const DEFAULT_CFG = { color: '#616161', icon: 'swap_horiz', bg: '#f5f5f5' };

@Component({
  selector: 'app-mouvements',
  templateUrl: './mouvements.component.html',
  styleUrls: ['./mouvements.component.scss']
})
export class MouvementsComponent implements OnInit {

  allMouvements: Mouvement[] = [];
  filtered: Mouvement[] = [];
  groups: TimelineGroup[] = [];

  loading = false;
  error = '';
  searchText = '';
  activeFilter = 'ALL';

  get totalCount(): number { return this.allMouvements.length; }

  get monthCount(): number {
    const now = new Date();
    return this.allMouvements.filter(m => {
      const d = new Date(m.dateMouvement);
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    }).length;
  }

  get uniqueTypes(): string[] {
    return Array.from(new Set(this.allMouvements.map(m => m.type)));
  }

  getTypeCount(type: string): number {
    return this.allMouvements.filter(m => m.type === type).length;
  }

  constructor(private svc: MouvementService, private router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error = '';
    this.svc.listAll().subscribe({
      next: (data) => {
        this.allMouvements = data || [];
        this.applyFilters();
      },
      error: (err) => {
        this.error = 'Erreur HTTP ' + (err?.status || '') + ': ' + (err?.message || 'inconnue');
      }
    });
  }

  setFilter(type: string): void {
    this.activeFilter = type;
    this.applyFilters();
  }

  onSearch(text: string): void {
    this.searchText = text;
    this.applyFilters();
  }

  private applyFilters(): void {
    let list = [...this.allMouvements];
    if (this.activeFilter !== 'ALL') {
      list = list.filter(m => m.type === this.activeFilter);
    }
    const q = (this.searchText || '').toLowerCase().trim();
    if (q) {
      list = list.filter(m =>
        (m.voitureLabel || '').toLowerCase().includes(q) ||
        (m.typeLabel || m.type || '').toLowerCase().includes(q) ||
        (m.commentaire || '').toLowerCase().includes(q)
      );
    }
    this.filtered = list;
    this.buildGroups(list);
  }

  private buildGroups(list: Mouvement[]): void {
    const now = new Date();
    const todayStr = now.toISOString().slice(0, 10);
    const yest = new Date(now.getTime() - 86400000).toISOString().slice(0, 10);
    const weekAgo = new Date(now.getTime() - 7 * 86400000);
    const monthAgo = new Date(now.getTime() - 30 * 86400000);

    const b: Record<string, Mouvement[]> = { today: [], yesterday: [], week: [], month: [], older: [] };
    list.forEach(m => {
      const d = new Date(m.dateMouvement);
      const ds = d.toISOString().slice(0, 10);
      if (ds === todayStr) { b['today'].push(m); }
      else if (ds === yest) { b['yesterday'].push(m); }
      else if (d >= weekAgo) { b['week'].push(m); }
      else if (d >= monthAgo) { b['month'].push(m); }
      else { b['older'].push(m); }
    });

    this.groups = [
      { label: "Aujourd'hui", mouvements: b['today'] },
      { label: 'Hier', mouvements: b['yesterday'] },
      { label: 'Cette semaine', mouvements: b['week'] },
      { label: 'Ce mois', mouvements: b['month'] },
      { label: 'Plus ancien', mouvements: b['older'] }
    ].filter(g => g.mouvements.length > 0);
  }

  getTypeConfig(type: string): { color: string; icon: string; bg: string } {
    return TYPE_CONFIG[type] || DEFAULT_CFG;
  }

  goToVoiture(id: number | undefined): void {
    if (id) { this.router.navigate(['/voitures', id]); }
  }

  formatTime(iso: string): string {
    try { return new Date(iso).toLocaleTimeString('fr-CA', { hour: '2-digit', minute: '2-digit' }); }
    catch (e) { return ''; }
  }

  formatDate(iso: string): string {
    try { return new Date(iso).toLocaleDateString('fr-CA', { day: '2-digit', month: 'short', year: 'numeric' }); }
    catch (e) { return iso; }
  }
}
