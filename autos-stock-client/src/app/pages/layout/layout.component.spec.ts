import {of} from 'rxjs';
import {LayoutComponent} from './layout.component';

/**
 * Le prenom affiche sous l'icone de profil est tire du champ nom : il n'existe
 * pas de champ prenom distinct dans le modele utilisateur.
 */
describe('LayoutComponent — prenom affiche', () => {

  const creer = (user: any) => {
    const auth: any = {
      currentUser: user,
      isAdmin: () => false,
      isSuperAdmin: () => false,
    };
    const bp: any = {observe: () => of({matches: false})};
    return new LayoutComponent(bp, auth, {} as any, {} as any, {current: 'fr'} as any);
  };

  it('garde le premier mot d un nom compose', () => {
    expect(creer({nom: 'Joel Stephane Pangop'}).prenom).toBe('Joel');
  });

  it('rend le nom tel quel s il ne compte qu un mot', () => {
    expect(creer({nom: 'Tedga'}).prenom).toBe('Tedga');
  });

  it('ignore les espaces superflus', () => {
    expect(creer({nom: '   Joel   Pangop '}).prenom).toBe('Joel');
  });

  it('n affiche rien sans utilisateur connecte', () => {
    expect(creer(null).prenom).toBe('');
  });

  it('n affiche rien si le nom est vide', () => {
    expect(creer({nom: '   '}).prenom).toBe('');
  });
});
