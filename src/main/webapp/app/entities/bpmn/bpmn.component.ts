import {Component, HostListener, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FlowService} from "../flow/service/flow.service";
import {IFlow} from "../flow/flow.model";
import {ApplicationConfigService} from "../../core/config/application-config.service";

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
              public applicationConfigService: ApplicationConfigService) { }

  ngOnInit(): void {

    this.flowId = this.route.snapshot.paramMap.get('flowId');
    this.flowService.find(this.flowId).subscribe(res => this.flow = res.body);

    this.iframe = document.getElementById("bpmnFrame");

    this.iframe.addEventListener("load", () => {
      this.iframe.contentWindow.postMessage(this.flow?.flow ,this.bpmnUrl);
    });

  }

  @HostListener('window:message',['$event'])
  resieveXmlFromBPMN(e:any):any {
    if (e.origin!=this.bpmnUrl) {
      debugger;
      return;
    }
    let aa = e;
    debugger;
  }

}
