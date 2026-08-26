import { assign } from 'min-dash'
import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider'
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory'
import Create from 'diagram-js/lib/features/create/Create'
import SpaceTool from 'diagram-js/lib/features/space-tool/SpaceTool'
import LassoTool from 'diagram-js/lib/features/lasso-tool/LassoTool'
import HandTool from 'diagram-js/lib/features/hand-tool/HandTool'
import GlobalConnect from 'diagram-js/lib/features/global-connect/GlobalConnect'
import Palette from 'diagram-js/lib/features/palette/Palette'

class EnhancementPaletteProvider extends PaletteProvider {
  private readonly _palette: Palette
  private readonly _create: Create
  private readonly _elementFactory: ElementFactory
  private readonly _spaceTool: SpaceTool
  private readonly _lassoTool: LassoTool
  private readonly _handTool: HandTool
  private readonly _globalConnect: GlobalConnect
  private readonly _translate: any
  constructor(
    palette,
    create,
    elementFactory,
    spaceTool,
    lassoTool,
    handTool,
    globalConnect,
    translate
  ) {
    super(
      palette,
      create,
      elementFactory,
      spaceTool,
      lassoTool,
      handTool,
      globalConnect,
      translate
    )
    this._palette = palette
    this._create = create
    this._elementFactory = elementFactory
    this._spaceTool = spaceTool
    this._lassoTool = lassoTool
    this._handTool = handTool
    this._globalConnect = globalConnect
    this._translate = translate
  }
  getPaletteEntries() {
    const actions = {},
      create = this._create,
      elementFactory = this._elementFactory,
      translate = this._translate

    function createAction(
      type: string,
      group: string,
      className: string,
      title: string,
      options?: Object
    ) {
      function createListener(event) {
        const shape = elementFactory.createShape(assign({ type: type }, options))

        if (options) {
          !shape.businessObject.di && (shape.businessObject.di = {})
          shape.businessObject.di.isExpanded = (options as { [key: string]: any }).isExpanded
        }

        create.start(event, shape)
      }

      const shortType = type.replace(/^bpmn:/, '')

      return {
        group: group,
        className: className,
        title: title || translate('Create {type}', { type: shortType }),
        action: {
          dragstart: createListener,
          click: createListener
        }
      }
    }

    function createSqlTask(event) {
      const sqlTask = elementFactory.createShape({ type: 'miyue:SqlTask' })
      create.start(event, sqlTask)
    }
    // function createTransformerModule(event) {
    //   const transformerModule = elementFactory.createShape({ type: 'Transformer:Transformer' })
    //   create.start(event, transformerModule)
    // }
    function createKafkaTransmitterModule(event) {
      const kafkaTransmitterModule = elementFactory.createShape({
        type: 'KafkaTransmitter:KafkaTransmitter'
      })
      create.start(event, kafkaTransmitterModule)
    }
    function createKafkaReceiverModule(event) {
      const kafkaReceiverModule = elementFactory.createShape({
        type: 'KafkaReceiver:KafkaReceiver'
      })
      create.start(event, kafkaReceiverModule)
    }
    function createHttpTransmitter(event) {
      const httpTransmitterModule = elementFactory.createShape({
        type: 'HttpTransmitter:HttpTransmitter'
      })
      create.start(event, httpTransmitterModule)
    }
    function createHttpReceiver(event) {
      const httpReceiverModule = elementFactory.createShape({ type: 'HttpReceiver:HttpReceiver' })
      create.start(event, httpReceiverModule)
    }
    function createFileTransmitter(event) {
      const fileTransmitterModule = elementFactory.createShape({
        type: 'FileTransmitter:FileTransmitter'
      })
      create.start(event, fileTransmitterModule)
    }
    function createFileReceiver(event) {
      const fileReceiverModule = elementFactory.createShape({ type: 'FileReceiver:FileReceiver' })
      create.start(event, fileReceiverModule)
    }
    function createDbTransmitter(event) {
      const dbTransmitterModule = elementFactory.createShape({
        type: 'DbTransmitter:DbTransmitter'
      })
      create.start(event, dbTransmitterModule)
    }
    function createEventaDbReceiver(event) {
      const eventaDbReceiverModule = elementFactory.createShape({
        type: 'EventaDbReceiver:EventaDbReceiver'
      })
      create.start(event, eventaDbReceiverModule)
    }
    function createFragmenter(event) {
      const eventaDbReceiverModule = elementFactory.createShape({
        type: 'Fragmenter:Fragmenter'
      })
      create.start(event, eventaDbReceiverModule)
    }
    function createCdrParser(event) {
      const cdrParserModule = elementFactory.createShape({ type: 'CdrParser:CdrParser' })
      create.start(event, cdrParserModule)
    }
    function createCsvTransformerCorner(event) {
      const csvTransformerCorner = elementFactory.createShape({
        type: 'CsvTransformer:CsvTransformer'
      })
      create.start(event, csvTransformerCorner)
    }
    function createFragmenterCorner(event) {
      const fragmenterCorner = elementFactory.createShape({ type: 'Fragmenter:Fragmenter' })
      create.start(event, fragmenterCorner)
    }
    function createHttpReceiverEventaCorner(event) {
      const httpReceiverEventaCorner = elementFactory.createShape({
        type: 'HttpReceiverEventa:HttpReceiverEventa'
      })
      create.start(event, httpReceiverEventaCorner)
    }
    function createDbReceiver(event) {
      const dbReceiverModule = elementFactory.createShape({ type: 'DbReceiver:DbReceiver' })
      create.start(event, dbReceiverModule)
    }
    function createMergerCorner(event) {
      const mergerCorner = elementFactory.createShape({ type: 'Merger:Merger' })
      create.start(event, mergerCorner)
    }

    assign(actions, {
      'create.merger-module': {
        group: 'activity',
        className: 'merger-module',
        title: 'Merger Module',
        action: {
          click: createMergerCorner,
          dragstart: createMergerCorner
        }
      },
      'create.fragmenter-module': {
        group: 'activity',
        className: 'fragmenter-module',
        title: 'Fragmenter Module',
        action: {
          click: createFragmenter,
          dragstart: createFragmenter
        }
      },
      // 'create.transformer-module': {
      //   group: 'activity',
      //   className: 'transformer-module',
      //   title: 'Transformer Module',
      //   action: {
      //     click: createTransformerModule,
      //     dragstart: createTransformerModule
      //   }
      // },
      'create.KafkaReceiver-module': {
        group: 'activity',
        className: 'KafkaReceiver-module',
        title: 'Kafka Receiver Module',
        action: {
          click: createKafkaReceiverModule,
          dragstart: createKafkaReceiverModule
        }
      },
      'create.KafkaTransmitter-module': {
        group: 'activity',
        className: 'KafkaTransmitter-module',
        title: 'Kafka Transmitter Module',
        action: {
          click: createKafkaTransmitterModule,
          dragstart: createKafkaTransmitterModule
        }
      },
      'create.HttpReceiver-module': {
        group: 'activity',
        className: 'HttpReceiver-module',
        title: 'Http Receiver Module',
        action: {
          click: createHttpReceiver,
          dragstart: createHttpReceiver
        }
      },
      'create.HttpTransmitter-module': {
        group: 'activity',
        className: 'HttpTransmitter-module',
        title: 'Http Transmitter Module',
        action: {
          click: createHttpTransmitter,
          dragstart: createHttpTransmitter
        }
      },
      'create.fileReceiver-module': {
        group: 'activity',
        className: 'fileReceiver-module',
        title: 'File Receiver Module',
        action: {
          click: createFileReceiver,
          dragstart: createFileReceiver
        }
      },
      'create.FileTransmitter-module': {
        group: 'activity',
        className: 'fileTransmitter-module',
        title: 'File Transmitter Module',
        action: {
          click: createFileTransmitter,
          dragstart: createFileTransmitter
        }
      },
      'create.dbReceiver-module': {
        group: 'activity',
        className: 'dbReceiver-module',
        title: 'DB Receiver Module',
        action: {
          click: createDbReceiver,
          dragstart: createDbReceiver
        }
      },
      'create.dbTransmitter-module': {
        group: 'activity',
        className: 'dbTransmitter-module',
        title: 'DB Transmitter Module',
        action: {
          click: createDbTransmitter,
          dragstart: createDbTransmitter
        }
      },
      // 'create.eventaDbReceiver-module': {
      //   group: 'activity',
      //   className: 'eventaDbReceiver-module',
      //   title: 'Eventa DB Receiver Module',
      //   action: {
      //     click: createEventaDbReceiver,
      //     dragstart: createEventaDbReceiver
      //   }
      // },
      'create.cdrParser-module': {
        group: 'activity',
        className: 'bpmn-icon-cdrParserModule',
        title: 'CDR Parser Module',
        action: {
          click: createCdrParser,
          dragstart: createCdrParser
        }
      },
      'create.csvTransformerCorner-module': {
        group: 'activity',
        className: 'csvTransformer-module',
        title: 'CSV Transformer Module',
        action: {
          click: createCsvTransformerCorner,
          dragstart: createCsvTransformerCorner
        }
      }
    })

    return actions
  }
}

EnhancementPaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'translate'
]

export default EnhancementPaletteProvider
