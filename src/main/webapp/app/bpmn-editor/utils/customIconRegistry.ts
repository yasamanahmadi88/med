import { ref, reactive } from 'vue'

export interface CustomIcon {
  id: string
  name: string
  svgContent: string
  parameters: Record<string, any>
  className: string
  type: string
}

class CustomIconRegistry {
  private icons = reactive<Map<string, CustomIcon>>(new Map())
  private storageKey = 'custom-bpmn-icons'

  constructor() {
    this.loadFromStorage()
  }

  private loadFromStorage() {
    try {
      const stored = localStorage.getItem(this.storageKey)
      if (stored) {
        const icons = JSON.parse(stored)
        Object.entries(icons).forEach(([id, icon]) => {
          this.icons.set(id, icon as CustomIcon)
        })
      }
    } catch (error) {
      console.error('Failed to load custom icons from storage:', error)
    }
  }

  private saveToStorage() {
    try {
      const iconsObj = Object.fromEntries(this.icons)
      localStorage.setItem(this.storageKey, JSON.stringify(iconsObj))
    } catch (error) {
      console.error('Failed to save custom icons to storage:', error)
    }
  }

  addIcon(icon: Omit<CustomIcon, 'id' | 'className' | 'type'>): string {
    const id = `custom-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    const className = `custom-icon-${id}`
    const type = `Custom:${icon.name.replace(/\s+/g, '')}`

    const customIcon: CustomIcon = {
      ...icon,
      id,
      className,
      type
    }

    this.icons.set(id, customIcon)
    this.saveToStorage()
    this.generateCSS(className, icon.svgContent)
    
    return id
  }

  removeIcon(id: string) {
    this.icons.delete(id)
    this.saveToStorage()
    this.removeCSS(`custom-icon-${id}`)
  }

  getIcons(): CustomIcon[] {
    return Array.from(this.icons.values())
  }

  getIcon(id: string): CustomIcon | undefined {
    return this.icons.get(id)
  }

  private generateCSS(className: string, svgContent: string) {
    const styleId = `style-${className}`
    let styleElement = document.getElementById(styleId) as HTMLStyleElement
    
    if (!styleElement) {
      styleElement = document.createElement('style')
      styleElement.id = styleId
      document.head.appendChild(styleElement)
    }

    const svgDataUrl = `data:image/svg+xml;base64,${btoa(svgContent)}`
    
    styleElement.textContent = `
      .${className} {
        background-image: url("${svgDataUrl}");
        background-size: 1em 1em;
        background-position: center center;
        background-repeat: no-repeat;
      }
      
      .${className}:active {
        background-image: url("${svgDataUrl}");
        background-size: 1.3em 1.3em;
        background-position: center center;
        background-repeat: no-repeat;
      }
      
      .${className}::selection {
        background-image: url("${svgDataUrl}");
        background-size: 1.3em 1.3em;
        background-position: center center;
        background-repeat: no-repeat;
      }
    `
  }

  private removeCSS(className: string) {
    const styleId = `style-${className}`
    const styleElement = document.getElementById(styleId)
    if (styleElement) {
      styleElement.remove()
    }
  }

  updateIconParameters(id: string, parameters: Record<string, any>) {
    const icon = this.icons.get(id)
    if (icon) {
      icon.parameters = { ...icon.parameters, ...parameters }
      this.icons.set(id, icon)
      this.saveToStorage()
    }
  }
}

export const customIconRegistry = new CustomIconRegistry()
