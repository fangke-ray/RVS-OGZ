using MySql.Data.MySqlClient;
using System;
using System.ComponentModel;
using System.Windows.Forms;

namespace AngleModelCollector
{
	public class DBAccess : UserControl
	{
		private static MySqlConnection connection;

		private IContainer components;

		public DBAccess()
		{
			this.InitializeComponent();
		}

		public static void initDB()
		{
			const string connectionString = "Data Source=10.220.142.227;port=3306;Database=rvsdb;User Id=dbma;Password=FEDur7s#;CharSet=utf8";
			DBAccess.connection = new MySqlConnection(connectionString);
		}

		public static bool OpenConnection()
		{
			bool result;
			try
			{
				if (DBAccess.connection == null)
				{
					DBAccess.initDB();
				}
				DBAccess.connection.Open();
				result = true;
			}
			catch (MySqlException ex)
			{
				int number = ex.Number;
				if (number != 0)
				{
					if (number == 1045)
					{
						MessageBox.Show("Invalid username/password, please try again");
					}
				}
				else
				{
					MessageBox.Show("Cannot connect to server.  Contact administrator");
				}
				result = false;
			}
			return result;
		}

		public static bool CloseConnection()
		{
			bool result;
			try
			{
				DBAccess.connection.Close();
				result = true;
			}
			catch (MySqlException ex)
			{
				MessageBox.Show(ex.Message);
				result = false;
			}
			return result;
		}

		public static string[] SelectModelAngelByMaterialId(string materialID)
		{
			string cmdText = "select m.omr_notifi_no as sorc_no, m.serial_no, mdl.name as model_name, ma.access_times from material m join model mdl \ton m.model_id = mdl.model_id left join model_angel ma \ton m.model_id = ma.model_id where material_id = " + materialID + ";";
			var array = new string[4];
			if (DBAccess.OpenConnection())
			{
				var mySqlCommand = new MySqlCommand(cmdText, DBAccess.connection);
				MySqlDataReader mySqlDataReader = mySqlCommand.ExecuteReader();
				if (mySqlDataReader.Read())
				{
					array[0] = string.Concat(mySqlDataReader["sorc_no"]);
					array[1] = string.Concat(mySqlDataReader["serial_no"]);
					array[2] = string.Concat(mySqlDataReader["model_name"]);
					array[3] = string.Concat(mySqlDataReader["access_times"]);
				}
				mySqlDataReader.Close();
				DBAccess.CloseConnection();
				return array;
			}
			return array;
		}

		protected override void Dispose(bool disposing)
		{
			if (disposing && this.components != null)
			{
				this.components.Dispose();
			}
			base.Dispose(disposing);
		}

		private void InitializeComponent()
		{
			base.AutoScaleMode = AutoScaleMode.Font;
			base.Name = "DBAccess";
		}
	}
}
