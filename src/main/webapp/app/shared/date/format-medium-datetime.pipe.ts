import { Pipe, PipeTransform } from '@angular/core';

import dayjs from 'dayjs/esm';

@Pipe({
  name: 'formatMediumDatetime',
  standalone: false,
})
export class FormatMediumDatetimePipe implements PipeTransform {
  transform(day: dayjs.Dayjs | null | undefined): string {
    return day ? day.format('D/MM/YYYY HH:mm:ss') : '';
  }
}
