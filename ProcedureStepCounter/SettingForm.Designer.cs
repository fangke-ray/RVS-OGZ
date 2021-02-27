/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-27
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
		private System.Windows.Forms.Label lbl_rvs_server_ip;
		private System.Windows.Forms.Label lbl_rvs_server_name;
		private System.Windows.Forms.Label lbl_local_ip;
		private System.Windows.Forms.TextBox txt_rvs_server_ip;
		private System.Windows.Forms.TextBox txt_rvs_server_name;
		private System.Windows.Forms.TextBox txt_local_ip;
		private System.Windows.Forms.Label lbl_process_code;
		private System.Windows.Forms.TextBox txt_process_code;
		private System.Windows.Forms.RadioButton rad_px_a;
		private System.Windows.Forms.RadioButton rad_px_b;
		
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
		private void InitializeComponent()
		{
			this.ComSetGrid = new System.Windows.Forms.DataGridView();
			this.WarnLabel = new System.Windows.Forms.Label();
			this.lbl_rvs_server_ip = new System.Windows.Forms.Label();
			this.lbl_rvs_server_name = new System.Windows.Forms.Label();
			this.lbl_local_ip = new System.Windows.Forms.Label();
			this.txt_rvs_server_ip = new System.Windows.Forms.TextBox();
			this.txt_rvs_server_name = new System.Windows.Forms.TextBox();
			this.txt_local_ip = new System.Windows.Forms.TextBox();
			this.lbl_process_code = new System.Windows.Forms.Label();
			this.txt_process_code = new System.Windows.Forms.TextBox();
			this.rad_px_a = new System.Windows.Forms.RadioButton();
			this.rad_px_b = new System.Windows.Forms.RadioButton();
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
			// lbl_rvs_server_ip
			// 
			this.lbl_rvs_server_ip.Font = new System.Drawing.Font("宋体", 10.5F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.lbl_rvs_server_ip.Location = new System.Drawing.Point(27, 57);
			this.lbl_rvs_server_ip.Name = "lbl_rvs_server_ip";
			this.lbl_rvs_server_ip.Size = new System.Drawing.Size(98, 23);
			this.lbl_rvs_server_ip.TabIndex = 2;
			this.lbl_rvs_server_ip.Text = "RVS 服务器 IP";
			this.lbl_rvs_server_ip.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// lbl_rvs_server_name
			// 
			this.lbl_rvs_server_name.Font = new System.Drawing.Font("宋体", 10.5F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.lbl_rvs_server_name.Location = new System.Drawing.Point(27, 88);
			this.lbl_rvs_server_name.Name = "lbl_rvs_server_name";
			this.lbl_rvs_server_name.Size = new System.Drawing.Size(98, 23);
			this.lbl_rvs_server_name.TabIndex = 3;
			this.lbl_rvs_server_name.Text = "RVS 服务名";
			this.lbl_rvs_server_name.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// lbl_local_ip
			// 
			this.lbl_local_ip.Font = new System.Drawing.Font("宋体", 10.5F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.lbl_local_ip.Location = new System.Drawing.Point(27, 140);
			this.lbl_local_ip.Name = "lbl_local_ip";
			this.lbl_local_ip.Size = new System.Drawing.Size(98, 23);
			this.lbl_local_ip.TabIndex = 4;
			this.lbl_local_ip.Text = "本机 IP";
			this.lbl_local_ip.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// txt_rvs_server_ip
			// 
			this.txt_rvs_server_ip.Location = new System.Drawing.Point(144, 60);
			this.txt_rvs_server_ip.Name = "txt_rvs_server_ip";
			this.txt_rvs_server_ip.Size = new System.Drawing.Size(228, 21);
			this.txt_rvs_server_ip.TabIndex = 5;
			this.txt_rvs_server_ip.TextChanged += new System.EventHandler(this.BaseTextChanged);
			// 
			// txt_rvs_server_name
			// 
			this.txt_rvs_server_name.Location = new System.Drawing.Point(144, 89);
			this.txt_rvs_server_name.Name = "txt_rvs_server_name";
			this.txt_rvs_server_name.Size = new System.Drawing.Size(228, 21);
			this.txt_rvs_server_name.TabIndex = 6;
			this.txt_rvs_server_name.TextChanged += new System.EventHandler(this.BaseTextChanged);
			// 
			// txt_local_ip
			// 
			this.txt_local_ip.Location = new System.Drawing.Point(90, 143);
			this.txt_local_ip.Name = "txt_local_ip";
			this.txt_local_ip.Size = new System.Drawing.Size(85, 21);
			this.txt_local_ip.TabIndex = 7;
			this.txt_local_ip.TextChanged += new System.EventHandler(this.BaseTextChanged);
			// 
			// lbl_process_code
			// 
			this.lbl_process_code.Font = new System.Drawing.Font("宋体", 10.5F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.lbl_process_code.Location = new System.Drawing.Point(190, 140);
			this.lbl_process_code.Name = "lbl_process_code";
			this.lbl_process_code.Size = new System.Drawing.Size(78, 23);
			this.lbl_process_code.TabIndex = 8;
			this.lbl_process_code.Text = "本机工位号";
			this.lbl_process_code.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// txt_process_code
			// 
			this.txt_process_code.Location = new System.Drawing.Point(274, 143);
			this.txt_process_code.Name = "txt_process_code";
			this.txt_process_code.Size = new System.Drawing.Size(62, 21);
			this.txt_process_code.TabIndex = 9;
			this.txt_process_code.TextChanged += new System.EventHandler(this.BaseTextChanged);

			// 
			// rad_px_a
			// 
			this.rad_px_a.Location = new System.Drawing.Point(343, 143);
			this.rad_px_a.Name = "rad_px_a";
			this.rad_px_a.Size = new System.Drawing.Size(52, 24);
			this.rad_px_a.TabIndex = 10;
			this.rad_px_a.TabStop = true;
			this.rad_px_a.Text = "A 线";
			this.rad_px_a.UseVisualStyleBackColor = true;
			this.rad_px_a.Click += new System.EventHandler(this.Rad_px_Click);
			this.rad_px_a.CheckedChanged += new System.EventHandler(this.Rad_px_CheckedChanged);

			// 
			// rad_px_b
			// 
			this.rad_px_b.Location = new System.Drawing.Point(394, 143);
			this.rad_px_b.Name = "rad_px_b";
			this.rad_px_b.Size = new System.Drawing.Size(52, 24);
			this.rad_px_b.TabIndex = 10;
			this.rad_px_b.TabStop = true;
			this.rad_px_b.Text = "B 线";
			this.rad_px_b.UseVisualStyleBackColor = true;
			this.rad_px_b.Click += new System.EventHandler(this.Rad_px_Click);
			this.rad_px_b.CheckedChanged += new System.EventHandler(this.Rad_px_CheckedChanged);
			// 
			// SettingForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(484, 362);
			this.Controls.Add(this.rad_px_b);
			this.Controls.Add(this.rad_px_a);
			this.Controls.Add(this.txt_process_code);
			this.Controls.Add(this.lbl_process_code);
			this.Controls.Add(this.txt_local_ip);
			this.Controls.Add(this.txt_rvs_server_name);
			this.Controls.Add(this.txt_rvs_server_ip);
			this.Controls.Add(this.lbl_local_ip);
			this.Controls.Add(this.lbl_rvs_server_name);
			this.Controls.Add(this.lbl_rvs_server_ip);
			this.Controls.Add(this.WarnLabel);
			this.Controls.Add(this.ComSetGrid);
			this.Name = "SettingForm";
			this.Text = "配置";
			this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Check_And_Update);
			this.Shown += new System.EventHandler(this.SettingShown);
			((System.ComponentModel.ISupportInitialize)(this.ComSetGrid)).EndInit();
			this.ResumeLayout(false);
			this.PerformLayout();

		}// SettingForm
	}
}
