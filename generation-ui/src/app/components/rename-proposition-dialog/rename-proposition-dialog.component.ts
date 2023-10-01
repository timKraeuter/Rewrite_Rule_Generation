import { Component, HostListener, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Proposition } from '../../services/proposition.service';

@Component({
    selector: 'app-rename-proposition-dialog',
    templateUrl: './rename-proposition-dialog.component.html',
    styleUrls: ['./rename-proposition-dialog.component.scss'],
})
export class RenamePropositionDialogComponent {
    // TODO: Check groove and make sure the name is allowed in CTL (see angular error state)
    public newName: string;

    constructor(
        private dialogRef: MatDialogRef<RenamePropositionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: RenamePropositionDialogData,
    ) {
        this.newName = data.proposition.name;
    }

    closeDialog() {
        this.dialogRef.close();
    }
    @HostListener('window:keyup.Enter', ['$event'])
    onDialogClick(event: KeyboardEvent): void {
        this.saveNameAndCloseDialog();
    }

    saveNameAndCloseDialog() {
        this.data.proposition.name = this.newName;
        this.closeDialog();
    }
}

export interface RenamePropositionDialogData {
    proposition: Proposition;
}