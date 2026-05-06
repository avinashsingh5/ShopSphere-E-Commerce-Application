import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { Category } from '../../core/models/catalog.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './admin-categories.component.html',
  styleUrl: './admin-categories.component.css'
})
export class AdminCategoriesComponent implements OnInit {
  categories: Category[] = [];

  loading = true;
  saving = false;
  showModal = false;

  editingCategory: Category | null = null;

  categoryForm: FormGroup;

  successMessage = '';
  errorMessage = '';

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.errorMessage = '';

    this.adminService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load categories';
      }
    });
  }

  openAddModal(): void {
    this.editingCategory = null;
    this.errorMessage = '';
    this.categoryForm.reset({
      name: '',
      description: ''
    });
    this.showModal = true;
  }

  openEditModal(category: Category): void {
    this.editingCategory = category;
    this.errorMessage = '';

    this.categoryForm.patchValue({
      name: category.name,
      description: category.description
    });

    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingCategory = null;
    this.saving = false;
    this.errorMessage = '';
  }

  saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const data = {
      name: this.categoryForm.value.name,
      description: this.categoryForm.value.description || ''
    };

    if (this.editingCategory) {
      this.adminService.updateCategory(this.editingCategory.id, data).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadCategories();
          this.showSuccess('Category updated successfully');
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to update category';
        }
      });
    } else {
      this.adminService.createCategory(data).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadCategories();
          this.showSuccess('Category created successfully');
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to create category';
        }
      });
    }
  }

  deleteCategory(category: Category): void {
    const confirmed = confirm(`Are you sure you want to delete "${category.name}"?`);

    if (!confirmed) return;

    this.adminService.deleteCategory(category.id).subscribe({
      next: () => {
        this.loadCategories();
        this.showSuccess('Category deleted successfully');
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to delete category');
      }
    });
  }

  private showSuccess(message: string): void {
    this.successMessage = message;

    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }
}