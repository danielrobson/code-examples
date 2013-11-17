namespace RaveCatParser
{
    partial class RaveCat
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.txtLog = new System.Windows.Forms.TextBox();
            this.btnGetRaves = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // txtLog
            // 
            this.txtLog.Location = new System.Drawing.Point(13, 13);
            this.txtLog.Multiline = true;
            this.txtLog.Name = "txtLog";
            this.txtLog.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txtLog.Size = new System.Drawing.Size(440, 419);
            this.txtLog.TabIndex = 0;
            // 
            // btnGetRaves
            // 
            this.btnGetRaves.Location = new System.Drawing.Point(184, 449);
            this.btnGetRaves.Name = "btnGetRaves";
            this.btnGetRaves.Size = new System.Drawing.Size(75, 23);
            this.btnGetRaves.TabIndex = 1;
            this.btnGetRaves.Text = "Get Raves";
            this.btnGetRaves.UseVisualStyleBackColor = true;
            this.btnGetRaves.Click += new System.EventHandler(this.btnGetRaves_Click);
            // 
            // RaveCat
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(476, 493);
            this.Controls.Add(this.btnGetRaves);
            this.Controls.Add(this.txtLog);
            this.Name = "RaveCat";
            this.Text = "RaveCatParser";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.RaveCat_FormClosed);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox txtLog;
        private System.Windows.Forms.Button btnGetRaves;
    }
}

