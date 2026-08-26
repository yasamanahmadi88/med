import { assign } from 'min-dash'
import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider'
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory'
import Create from 'diagram-js/lib/features/create/Create'
import SpaceTool from 'diagram-js/lib/features/space-tool/SpaceTool'
import LassoTool from 'diagram-js/lib/features/lasso-tool/LassoTool'
import HandTool from 'diagram-js/lib/features/hand-tool/HandTool'
import GlobalConnect from 'diagram-js/lib/features/global-connect/GlobalConnect'
import Palette from 'diagram-js/lib/features/palette/Palette'
import { customIconRegistry } from '@/utils/customIconRegistry'

class CustomIconPaletteProvider extends PaletteProvider {
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
    const actions = {}
    const create = this._create
    const elementFactory = this._elementFactory
    const translate = this._translate

    // Get standard actions from parent
    const standardActions = super.getPaletteEntries()
    assign(actions, standardActions)

    // Add custom icons
    const customIcons = customIconRegistry.getIcons()
    
    customIcons.forEach(icon => {
      const actionKey = `create.custom-${icon.id}`
      
      const createCustomElement = (event: any) => {
        const customElement = elementFactory.createShape({
          type: icon.type,
          businessObject: {
            $type: icon.type,
            ...icon.parameters
          }
        })
        
        // Store custom parameters in the element
        Object.entries(icon.parameters).forEach(([key, value]) => {
          customElement.businessObject[key] = value
        })
        
        create.start(event, customElement)
      }

      assign(actions, {
        [actionKey]: {
          group: 'activity',
          className: icon.className,
          title: icon.name,
          action: {
            dragstart: createCustomElement,
            click: createCustomElement
          }
        }
      })
    })

    return actions
  }

  refresh() {
    // Refresh palette when custom icons change
    (this._palette as any)._update()
  }
}

CustomIconPaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'translate'
]

export default CustomIconPaletteProvider
