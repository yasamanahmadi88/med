import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ThemeMode, ThemeService } from './theme.service';

@Component({
  selector: 'jhi-theme-toggle',
  templateUrl: './theme-toggle.component.html',
  styleUrls: ['./theme-toggle.component.scss'],
  standalone: false,
})
export class ThemeToggleComponent implements OnInit, OnDestroy {
  theme: ThemeMode = 'light';
  private sub?: Subscription;

  constructor(private readonly themeService: ThemeService) {}

  ngOnInit(): void {
    this.theme = this.themeService.currentTheme;
    this.sub = this.themeService.theme$.subscribe(theme => {
      this.theme = theme;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  toggle(): void {
    this.themeService.toggleTheme();
  }

  get ariaLabel(): string {
    return this.theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme';
  }

  get tooltip(): string {
    return this.ariaLabel;
  }
}
