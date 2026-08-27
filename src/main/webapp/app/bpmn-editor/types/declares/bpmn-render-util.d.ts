// bpmn-js ships no types, and the project's bpmn.d.ts does not cover the render helpers the
// custom renderer uses. These are the shape/colour accessors bpmn-js's own BpmnRenderer builds
// on; they take diagram-js elements and return path strings, colours or booleans.
declare module 'bpmn-js/lib/draw/BpmnRenderUtil' {
  import { Base, Shape } from 'diagram-js/lib/model';

  export function getCirclePath(shape: Shape): string;
  export function getDiamondPath(shape: Shape): string;
  export function getRectPath(shape: Shape): string;
  export function getRoundRectPath(shape: Shape, borderRadius: number): string;

  export function getDi(element: Base): any;
  export function getSemantic(element: Base): any;

  /** The `defaultColor` fallbacks are what bpmn-js passes from its own renderer config. */
  export function getFillColor(element: Base, defaultColor?: string): string;
  export function getStrokeColor(element: Base, defaultColor?: string): string;
  export function getLabelColor(element: Base, defaultColor?: string, defaultStrokeColor?: string): string;

  export function isCollection(element: Base): boolean;
  export function isThrowEvent(element: Base): boolean;
  export function isTypedEvent(event: any, eventDefinitionType: string, filter?: any): boolean;
}
