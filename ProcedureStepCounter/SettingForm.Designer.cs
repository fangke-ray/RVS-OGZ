/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-17
 * 时间: 9:03
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
namespace ProcedureStepCounter
{
	partial class SettingForm
	{
		/// <summary>
		/// Designer variable used to keep track of non-visual components.
		/// </summary>
		private System.ComponentModel.IContainer components = null;
		private System.Windows.Forms.DataGridView ComSetGrid;
		private System.Windows.Forms.Label WarnLabel;
		
		/// <summary>
		/// Disposes resources used by the form.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing) {
				if (components != null) {
					components.Dispose();
				}
			}
			base.Dispose(disposing);
		}
		
		/// <summary>
		/// This method is required for Windows Forms designer support.
		/// Do not change the method contents inside the source code editor. The Forms designer might
		/// not be able to load this method if it was changed manually.
		/// </summary>
		private void InitializeComponent()
		{
			this.ComSetGrid = new System.Windows.Forms.DataGridView();
			this.WarnLabel = new System.Windows.Forms.Label();
			((System.ComponentModel.ISupportInitialize)(this.ComSetGrid)).BeginInit();
			this.SuspendLayout();
			// 
			// ComSetGrid
			// 
			this.ComSetGrid.AllowUserToAddRows = false;
			this.ComSetGrid.AllowUserToDeleteRows = false;
			this.ComSetGrid.AllowUserToResizeColumns = false;
			this.ComSetGrid.AllowUserToResizeRows = false;
			this.ComSetGrid.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
			this.ComSetGrid.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnEnter;
			this.ComSetGrid.Location = new System.Drawing.Point(33, 180);
			this.ComSetGrid.MultiSelect = false;
			this.ComSetGrid.Name = "ComSetGrid";
			this.ComSetGrid.RowTemplate.Height = 23;
			this.ComSetGrid.Size = new System.Drawing.Size(414, 152);
			this.ComSetGrid.TabIndex = 0;
			this.ComSetGrid.CellBeginEdit += new System.Windows.Forms.DataGridViewCellCancelEventHandler(this.SettingBeginEdit);
			this.ComSetGrid.CellValidated += new System.Windows.Forms.DataGridViewCellEventHandler(this.SettingValidated);
			this.ComSetGrid.CellValidating += new System.Windows.Forms.DataGridViewCellValidatingEventHandler(this.SettingValdation);
			// 
			// WarnLabel
			// 
			this.WarnLabel.Font = new System.Drawing.Font("微软雅黑", 14.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.WarnLabel.ForeColor = System.Drawing.Color.Brown;
			this.WarnLabel.Location = new System.Drawing.Point(36, 17);
			this.WarnLabel.Name = "WarnLabel";
			this.WarnLabel.Size = new System.Drawing.Size(410, 29);
			this.WarnLabel.TabIndex = 1;
			this.WarnLabel.Text = "请参照仪表端及连接端口，在此设置后使用。";
			this.WarnLabel.Visible = false;
			// 
			// SettingForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(484, 362);
			this.Controls.Add(this.WarnLabel);
			this.Controls.Add(this.ComSetGrid);
			this.Name = "SettingForm";
			this.Text = "配置";
			this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Check_And_Update);
			this.Shown += new System.EventHandler(this.SettingShown);
			((System.ComponentModel.ISupportInitialize)(this.ComSetGrid)).EndInit();
			this.ResumeLayout(false);

		}
	}
}
