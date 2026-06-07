import {PageVm} from "./PageVm";
import {DepenseDto} from "./DepenseDto";
import {DepenseMonthlyTotalDto} from "./DepenseMonthlyTotalDto";

export interface DepenseDashboardVm {
  total: number;
  totalPeriode?: number | null;
  page: PageVm<DepenseDto>;
  monthly?: DepenseMonthlyTotalDto[];
}
