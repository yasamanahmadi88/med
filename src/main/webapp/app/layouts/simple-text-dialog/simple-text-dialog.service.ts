import { Injectable } from '@angular/core';
import {SimpleTextDialogComponent} from "./simple-text-dialog.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Injectable({ providedIn: 'root' })
export class SimpleTextDialogService {

  constructor(protected modalService: NgbModal) {
  }

  open(text: String|null){
    const modalRef = this.modalService.open(SimpleTextDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.text = text;
  }
}
