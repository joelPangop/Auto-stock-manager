import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

const LANG_KEY = 'app_lang';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  readonly supported = ['fr', 'en'];

  constructor(private translate: TranslateService) {}

  init(): void {
    const saved = localStorage.getItem(LANG_KEY) || 'fr';
    this.translate.addLangs(this.supported);
    this.translate.setDefaultLang('fr');
    this.translate.use(saved);
  }

  use(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem(LANG_KEY, lang);
  }

  get current(): string {
    return this.translate.currentLang || 'fr';
  }
}
