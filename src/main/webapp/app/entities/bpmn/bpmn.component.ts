import {Component, HostListener, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FlowService} from "../flow/service/flow.service";
import {IFlow} from "../flow/flow.model";
import {ApplicationConfigService} from "../../core/config/application-config.service";
import {ToastrService} from "ngx-toastr";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'jhi-bpmn',
  templateUrl: './bpmn.component.html',
  styleUrls: ['./bpmn.component.scss']
})
export class BpmnComponent implements OnInit {

  flowId:any;
  flow: IFlow | null = null;
  iframe:any;
  bpmnUrl:string = "http://localhost";

  constructor(public route: ActivatedRoute,
              public flowService: FlowService,
              public applicationConfigService: ApplicationConfigService,
              public router: Router,
              private toastr: ToastrService,
              private translateService: TranslateService) { }

  ngOnInit(): void {

    this.flowId = this.route.snapshot.paramMap.get('flowId');
    this.flowService.find(this.flowId).subscribe(res => this.flow = res.body);

    this.iframe = document.getElementById("bpmnFrame");

    this.iframe.addEventListener("load", () => {
      this.iframe.contentWindow.postMessage(this.flow?.flow ,this.bpmnUrl);
    });

  }

  @HostListener('window:message', ['$event'])
  resieveXmlFromBPMN(e: any): any {
    if (e.origin == this.bpmnUrl) {

      if(e.data == "cancel") {
        this.router.navigate(['/flow']);
      }else{
        this.flowService.saveFlow(e.data).subscribe(res => {
          if (res.body.mediationStatus == "success") {
            this.toastr.success(this.translateService.instant("global.messages.info.saved"));
          }else{
            this.toastr.error(this.translateService.instant("error.internalServerError"));
          }
      });
        this.router.navigate(['/flow']);
      }
    }
  }

}
