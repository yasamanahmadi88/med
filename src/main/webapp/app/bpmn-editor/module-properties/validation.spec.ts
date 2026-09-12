import { describeProblem, moduleValidationProblems } from './validation';

/**
 * The Save gate reads this, so a miss here means an unrunnable flow reaches the backend and a
 * false positive means a valid one cannot be saved at all.
 */
describe('moduleValidationProblems', () => {
  /** A stand-in for a bpmn-js element: the registry hands back objects shaped like this. */
  const element = (id: string, type: string, properties: Record<string, unknown>, name?: string): any => {
    const businessObject: any = {
      $type: type,
      name,
      get: (property: string) => properties[property],
    };
    return { id, type, businessObject };
  };

  const modelerWith = (elements: any[], processEngine?: string): any => ({
    get(name: string) {
      if (name === 'elementRegistry') return { getAll: () => elements };
      if (name === 'config.processEngine') return processEngine;
      return undefined;
    },
  });

  it('finds nothing in an empty diagram', () => {
    expect(moduleValidationProblems(modelerWith([]))).toEqual([]);
  });

  it('reports an invalid ip with enough detail to find it', () => {
    const problems = moduleValidationProblems(
      modelerWith([element('FileTransmitter_1', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '999.1.1.1' }, 'Outbound')]),
    );

    expect(problems).toHaveLength(1);
    expect(problems[0]).toMatchObject({
      elementId: 'FileTransmitter_1',
      elementName: 'Outbound',
      fieldLabel: 'IP Address',
      message: 'Invalid IPv4 format (e.g., 192.168.1.1)',
    });
  });

  it('falls back to the element id when it has no label', () => {
    // An element the user has not named still has to be findable on the canvas.
    const problems = moduleValidationProblems(
      modelerWith([element('FileTransmitter_2', 'FileTransmitter:FileTransmitter', { 'camunda:port': '70000' })]),
    );

    expect(problems[0].elementName).toBe('FileTransmitter_2');
  });

  it('reads the property under the configured engine prefix', () => {
    // Module properties are namespaced by the process engine, so a flowable diagram stores
    // `flowable:ip`. Reading the camunda name there would silently validate nothing.
    const elements = [element('F', 'FileTransmitter:FileTransmitter', { 'flowable:ip': 'nonsense' })];

    expect(moduleValidationProblems(modelerWith(elements, 'flowable'))).toHaveLength(1);
    expect(moduleValidationProblems(modelerWith(elements, 'camunda'))).toEqual([]);
  });

  it('checks every validated field across every module', () => {
    const problems = moduleValidationProblems(
      modelerWith([
        element('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': 'bad', 'camunda:port': 'bad' }),
        element('H', 'HttpTransmitter:HttpTransmitter', { 'camunda:authUrl': 'a b' }),
        element('M', 'Merger:Merger', { 'camunda:expireTimeOfDay': '25:00:00' }),
      ]),
    );

    expect(problems.map(problem => problem.fieldLabel)).toEqual(['IP Address', 'Port', 'Auth Url', 'Expire Time Of Day']);
  });

  it('sees elements the user has never selected', () => {
    // This is the whole point of walking the registry: the Vue store only ever held the selected
    // element's errors, and cleared them when the selection moved on, so an invalid value on
    // another element left Save enabled.
    const problems = moduleValidationProblems(
      modelerWith([
        element('Ok', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '10.0.0.1' }),
        element('Bad', 'Merger:Merger', { 'camunda:expireTimeOfDay': 'noon' }),
      ]),
    );

    expect(problems.map(problem => problem.elementId)).toEqual(['Bad']);
  });

  it('ignores elements that are not integration modules', () => {
    expect(moduleValidationProblems(modelerWith([element('StartEvent_1', 'bpmn:StartEvent', {})]))).toEqual([]);
  });

  it('passes a valid diagram', () => {
    const problems = moduleValidationProblems(
      modelerWith([
        element('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '192.168.1.1', 'camunda:port': '8080' }),
        element('H', 'HttpTransmitter:HttpTransmitter', { 'camunda:authUrl': 'https://auth.example.com/token' }),
        element('M', 'Merger:Merger', { 'camunda:expireTimeOfDay': '23:59:59' }),
      ]),
    );

    expect(problems).toEqual([]);
  });

  it('returns nothing rather than throwing before the modeler exists', () => {
    // The toolbar renders in the same change-detection pass that builds the designer.
    expect(moduleValidationProblems(null)).toEqual([]);
    expect(moduleValidationProblems(undefined)).toEqual([]);
    expect(moduleValidationProblems({ get: () => undefined })).toEqual([]);
  });

  describe('describeProblem', () => {
    it('names the element, the field and the reason', () => {
      expect(
        describeProblem({
          elementId: 'F',
          elementName: 'Outbound',
          moduleLabel: 'File Transmitter',
          fieldLabel: 'Port',
          message: 'Port must be an integer between 0 and 65535',
        }),
      ).toBe('Outbound — Port: Port must be an integer between 0 and 65535');
    });
  });
});
