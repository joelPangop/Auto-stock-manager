export interface PageVm<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
