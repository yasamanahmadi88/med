import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'jhi-simple-text-dialog',
  templateUrl: './simple-text-dialog.component.html',
  styleUrls: ['./simple-text-dialog.component.scss']
})
export class SimpleTextDialogComponent implements OnInit {

  text:String = "";

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
  }


  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
