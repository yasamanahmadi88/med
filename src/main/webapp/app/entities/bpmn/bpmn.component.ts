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
  bpmnUrl:string = "*";
  flowXml : any;

  constructor(public route: ActivatedRoute,
              public flowService: FlowService,
              public applicationConfigService: ApplicationConfigService,
              public router: Router) { }

  ngOnInit(): void {

    this.flowId = this.route.snapshot.queryParams['flowId'];

    this.iframe = document.getElementById("bpmnFrame");

    if (this.flowId != undefined) {
      this.flowService.find(this.flowId).subscribe(res => {
        this.flow = res.body
        this.flowXml = this.flow?.flow;

        this.iframe.contentWindow?.postMessage(this.flowXml , "*"); // for when the iframe is already loaded

        this.iframe.addEventListener("load", () => {
          this.iframe.contentWindow.postMessage(this.flowXml , "*");
        });
      });
    } else {
      debugger
      this.flowXml = this.flowService.xmlTemp;

      this.iframe.contentWindow.postMessage(this.flowXml , "*"); // for when the iframe is already loaded

      this.iframe.addEventListener("load", () => {
        this.iframe.contentWindow.postMessage(this.flowXml , "*");
      });
    }
  }

  @HostListener('window:message', ['$event'])
  receiveXmlFromBPMN(e: any): any {
    if (e.data == "cancel") {
      window.history.back();
      if(this.flowService.xmlTemp == "")
        this.flowService.xmlTemp = " ";
    } else {
      if (this.flow) {
        this.flow.flow = e.data
        this.flowService.update(this.flow).subscribe();
      } else {
        this.flowService.xmlTemp = e.data;
      }
      window.history.back();
    }
  }

}
