import pyodbc

conn = pyodbc.connect('DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost;DATABASE=luckydraw;UID=sa;PWD=duy123')
cursor = conn.cursor()
cursor.execute("SELECT OBJECT_DEFINITION(OBJECT_ID('TRG_CAMPAIGN_VALIDATE'))")
row = cursor.fetchone()
if row:
    print(row[0])
