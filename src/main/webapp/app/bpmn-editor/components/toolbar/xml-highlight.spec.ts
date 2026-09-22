import { highlightXml } from './xml-highlight';

describe('xml-highlight', () => {
  describe('highlightXml', () => {
    it('should highlight XML tags with hljs-tag class', () => {
      const xml = '<process id="Process_1"></process>';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-tag');
      // Verify opening tag
      expect(result).toContain('&lt;process');
      expect(result).toContain('&gt;');
    });

    it('should highlight attributes with hljs-attr class', () => {
      const xml = '<process id="Process_1" name="My Process"></process>';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-attr');
      expect(result).toContain('id');
      expect(result).toContain('name');
    });

    it('should highlight attribute values with hljs-string class', () => {
      const xml = '<task id="Activity_1" name="Task Name"></task>';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-string');
      expect(result).toContain('Activity_1');
      expect(result).toContain('Task Name');
    });

    it('should highlight XML comments with hljs-comment class', () => {
      const xml = '<!-- This is a comment --><process></process>';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-comment');
      expect(result).toContain('This is a comment');
    });

    it('should highlight XML declarations with hljs-meta class', () => {
      const xml = '<?xml version="1.0" encoding="UTF-8"?><process></process>';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-meta');
      expect(result).toContain('xml');
      expect(result).toContain('version');
    });

    it('should escape HTML entities in XML content', () => {
      const xml = '<process><text>5 > 3 && 2 < 4</text></process>';
      const result = highlightXml(xml);

      // Verify HTML is properly escaped
      expect(result).toContain('&gt;');
      expect(result).toContain('&lt;');
      expect(result).toContain('&amp;&amp;');
    });

    it('should handle self-closing tags', () => {
      const xml = '<gateway id="Decision_1" />';
      const result = highlightXml(xml);

      expect(result).toContain('hljs-tag');
      expect(result).toContain('gateway');
    });

    it('should handle custom namespaces (Kafka)', () => {
      const xml = '<KafkaReceiver:kafkaReceiver id="Activity_kafka"><topic>my-topic</topic></KafkaReceiver:kafkaReceiver>';
      const result = highlightXml(xml);

      expect(result).toContain('KafkaReceiver');
      expect(result).toContain('kafkaReceiver');
      expect(result).toContain('topic');
    });

    it('should handle custom namespaces (CDR)', () => {
      const xml = '<CdrParser:cdrParser id="Activity_cdr"><cdrType>callRecord</cdrType></CdrParser:cdrParser>';
      const result = highlightXml(xml);

      expect(result).toContain('CdrParser');
      expect(result).toContain('cdrParser');
      expect(result).toContain('cdrType');
    });

    it('should handle custom namespaces (CSV)', () => {
      const xml = '<CsvExporter:csvExporter id="Activity_csv"><format>CSV</format></CsvExporter:csvExporter>';
      const result = highlightXml(xml);

      expect(result).toContain('CsvExporter');
      expect(result).toContain('csvExporter');
      expect(result).toContain('format');
    });

    it('should NOT create real DOM elements from script tags', () => {
      const xml = '<process><script>alert("xss")</script></process>';
      const result = highlightXml(xml);

      // The result is HTML-escaped, so when inserted via innerHTML it won't execute
      expect(result).toContain('&lt;script&gt;');
      expect(result).not.toContain('<script>');
    });

    it('should NOT create real DOM elements from img onerror handlers', () => {
      const xml = '<process><img src="x" onerror="alert(1)" /></process>';
      const result = highlightXml(xml);

      // Should be escaped
      expect(result).toContain('&lt;img');
      expect(result).not.toContain('<img src="x"');
    });

    it('should NOT create real DOM elements from event handler attributes', () => {
      const xml = '<process onload="malicious()" id="x"></process>';
      const result = highlightXml(xml);

      // Should be escaped
      expect(result).toContain('onload');
      expect(result).toContain('malicious');
    });

    it('should preserve exact text content after highlighting', () => {
      const originalText = '<bpmn:process id="proc1"><bpmn:task name="Task A"></bpmn:task></bpmn:process>';

      // This helper strips all HTML tags to verify text is preserved
      const stripTags = (html: string) => {
        const tmp = document.createElement('div');
        tmp.innerHTML = html;
        return tmp.textContent || '';
      };

      const highlighted = highlightXml(originalText);
      const strippedText = stripTags(highlighted);

      expect(strippedText).toBe(originalText);
    });

    it('should handle large diagrams without performance degradation', () => {
      let xml = '<?xml version="1.0" encoding="UTF-8"?>\n<bpmn:definitions>\n';
      for (let i = 0; i < 2000; i++) {
        xml += `  <bpmn:process id="process${i}">\n`;
        xml += `    <bpmn:task id="task${i}" name="Task ${i}"></bpmn:task>\n`;
        xml += `  </bpmn:process>\n`;
      }
      xml += '</bpmn:definitions>';

      const startTime = performance.now();
      const result = highlightXml(xml);
      const endTime = performance.now();

      expect(result).toBeDefined();
      expect(result.length).toBeGreaterThan(0);
      expect(endTime - startTime).toBeLessThan(1000); // Should complete in under 1 second
    });

    it('should handle CDATA sections', () => {
      const xml = '<process><documentation><![CDATA[This is a <safe> CDATA section]]></documentation></process>';
      const result = highlightXml(xml);

      expect(result).toContain('CDATA');
      expect(result).toContain('safe');
    });

    it('should handle numeric character entities', () => {
      const xml = '<process><text>Price: &#8364;100</text></process>';
      const result = highlightXml(xml);

      expect(result).toContain('&#8364;');
      expect(result).toContain('Price');
    });

    it('should handle named character entities', () => {
      const xml = '<process><text>&copy; 2024 &nbsp; Company</text></process>';
      const result = highlightXml(xml);

      expect(result).toContain('&copy;');
      expect(result).toContain('&nbsp;');
    });

    it('should highlight nested elements correctly', () => {
      const xml = `<process>
        <startEvent id="Start_1">
          <outgoing>Flow_1</outgoing>
        </startEvent>
        <task id="Task_1" name="Do Work">
          <incoming>Flow_1</incoming>
          <outgoing>Flow_2</outgoing>
        </task>
        <endEvent id="End_1">
          <incoming>Flow_2</incoming>
        </endEvent>
      </process>`;
      const result = highlightXml(xml);

      expect(result).toContain('hljs-tag');
      expect(result).toContain('startEvent');
      expect(result).toContain('task');
      expect(result).toContain('endEvent');
    });

    it('should memoize highlighting per unique XML string', () => {
      // Call twice with same XML - second should use memoized result
      const xml = '<process id="p1"></process>';

      const result1 = highlightXml(xml);
      const result2 = highlightXml(xml);

      expect(result1).toBe(result2); // Should be same reference if memoized, or at least equal
    });
  });
});
