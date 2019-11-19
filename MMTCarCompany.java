import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Date;			// used to represent date
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class MMTCarCompany {
	public static void main(String[] args) {
		// one will be the choice for user and Scanner is used to take input from keyboard
		String one;
		Scanner scan = new Scanner(System.in);
		
		/*
		 * username and password is stored in a variable as String which is used in getConnection() method
		*/
		String username = "spanchal";
		String password = "B00828070";
		while (true) {
			try {
				
				// User have to select from 1 or 2. 1 will let user to enter date and file name and 2 will exit 
				System.out.println("Press 1 to Enter Start Date, End Date and File name");
				System.out.println("Press 2 to Exit");
				System.out.println("Enter your choice :");
				one = scan.next();

				// Switch case is used to handle two case 
				switch (one) {

				case "1":
					// Class.forName() is used to register the driver class
					Class.forName("com.mysql.cj.jdbc.Driver");
					
					// getConnection() will establish connection with database 
					Connection con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC",
							username, password);
					
					// createStatement() will create the statement
					Statement st = con.createStatement();
					
					// execute() will execute the SQL statement	
					st.execute("use csci3901;");
					
					// fileName is where user want to write in xml format
					String fileName;

					System.out.println("Enter start date: ");
					String startDate = scan.next();
					
					// it will convert string startDate into sql date format i.e. "YYYY-MM-DD" 
					Date sDate = Date.valueOf(startDate);

					// if condition is used to check for exception conditions for start date
					if (sDate.equals(null) || sDate.equals(" ") || sDate.equals("/")) {

						throw new Exception();
					}

					System.out.println("Enter end date: ");
					String endDate = scan.next();
					
					// it will convert string endDate into sql date format i.e. "YYYY-MM-DD" 
					Date eDate = Date.valueOf(endDate);

					// if condition is used to check for exception conditions for end date
					if (eDate.equals(null) || eDate.equals("") || eDate.equals("/")) {
						throw new Exception();
					}

					// It will enter the file from the user
					System.out.println("Enter File Location : ");
					fileName = scan.next();
					fileName += scan.nextLine();
					fileName = fileName +".xml";
					
					//if the user enter null or file is empty or file has zero or one column then exception will be thrown
					if(fileName.equals("null") || fileName.isEmpty()) 
					{
						throw new Exception();
					}

					// BufferedWriter with FileWriter is used to take the file and write into it
					BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

					/* String sql will show columns with customer name, address, city , postal code, country,
					 * count number of orders and total order value.
					 * The first 1st sub query will multiply priceEach and quantityOrdered.
					 * The 2nd sub query will do the sum of multiplied columns.
					 * The outer query will count the the number of orders 
					 */
					String sql = "select sub2.customerName, sub2.addressLine1, sub2.city, sub2.postalCode,sub2.country, "
							+ "count(sub2.customerNumber) as Number_of_orders, sum(sub2.innersum)  as Total_Order_Value from\r\n"
							+ "(select sub1.customerNumber,sub1.customerName, sub1.addressLine1,sub1.addressLine2, sub1.city,"
							+ " sub1.state, sub1.postalCode, sub1.country, sub1.orderNumber, sum(sub1.mul) as innersum from\r\n"
							+ "(select c.customerName,  c.addressLine1, c.addressLine2, c.city, c.state, c.postalCode, c.country,"
							+ " o.customerNumber, od.orderNumber, od.quantityOrdered, od.priceEach, "
							+ "(od.priceEach * od.quantityOrdered) as mul \r\n"
							+ "from orderdetails od inner join orders o \r\n"
							+ " on o.orderNumber = od.orderNumber inner join customers as c "
							+ "on o.customerNumber = c.customerNumber\r\n"
							+ " where o.orderDate between '" + sDate + "' and '" + eDate + "') as sub1\r\n"
							+ "group by sub1.orderNumber) as sub2\r\n" + "group by sub2.customerNumber;";
					
					// ResultSet will hold all the data retrieved
					ResultSet rs1 = st.executeQuery(sql);
					
					// below is the code to print the xml tag in xml file
					bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
					bw.write("<year_end_summary>\n");

					bw.write("\t<year>\n");
					bw.write("\t\t<start_date>" + sDate + "</start_date>\n");
					bw.write("\t\t<end_date>" + eDate + "</end_date>\n");
					bw.write("\t</year>\n");
					bw.write("\t<customer_list>\n");

					while (rs1.next()) {
						
						// all the values that are retrieved from resultset are stored in string
						String CustomerName = rs1.getString("customerName");
						String C_AddressLine1 = rs1.getString("addressLine1");
						String C_City = rs1.getString("city");
						String C_PostalCode = rs1.getString("postalCode");
						String C_Country = rs1.getString("country");
						String C_Number_of_orders = rs1.getString("Number_of_orders");
						String C_Total_Order_Value = rs1.getString("Total_Order_Value");

						// all the variables are set between xml tags
						bw.write("\t\t<customer> \n");
						bw.write("\t\t\t<customer_name>" + CustomerName + "</customer_name>\n");
						bw.write("\t\t\t<address>\n");
						bw.write("\t\t\t\t<street_address>" + C_AddressLine1 + "</street_address>\n");
						bw.write("\t\t\t\t<city>" + C_City + "</city>\n");
						bw.write("\t\t\t\t<postal_code>" + C_PostalCode + "</postal_code>\n");
						bw.write("\t\t\t\t<country>" + C_Country + "</country>\n");
						bw.write("\t\t\t</address>\n");
						bw.write("\t\t\t<num_orders>" + C_Number_of_orders + "</num_orders>\n");
						bw.write("\t\t\t<order_value>" + C_Total_Order_Value + "</order_value>\n");

						bw.write("\t\t</customer> \n");

						bw.flush();
					
					}
					bw.write("\t</customer_list>\n");
				
					/*  sql2 will select product line, product name, product vendor, unit sold and total value of product sold 
					 * 	Inner query will multiply priceEach and quantityOrdered
					 *  Outer query will calculate the sum
					 */ 
					String sql2 = "select sub.productLine, sub.productName, sub.productVendor, sum(sub.quantityOrdered) "
							+ "as Unit_Sold, sum(sub.mul) as Total_Value_Of_Product_Sold from\r\n"
							+ "(select p.productCode,p.productLine, p.productName, p.productVendor,  od.quantityOrdered,"
							+ " od.priceEach, (od.priceEach * od.quantityOrdered) as mul from products p inner join orderdetails"
							+ " od on od.productCode = p.productCode\r\n"
							+ "inner join orders o on o.orderNumber = od.orderNumber where o.orderDate between '"
							+ sDate + "' and '" + eDate + "' \r\n" + ") as sub\r\n" + "group by productName";

					// ResultSet will hold all the data retrieved
					ResultSet rs2 = st.executeQuery(sql2);

					// below is the code to print the xml tag in xml file
					bw.write("\t<product_list>\n");
					while (rs2.next()) {

						// all the values that are retrieved from resultset are stored in string
						String ProductLine = rs2.getString("productLine");
						String ProductName = rs2.getString("productName");
						String ProductVendor = rs2.getString("productVendor");
						String Unit_Sold = rs2.getString("Unit_Sold");
						String Total_Value_Of_Product_Sold = rs2.getString("Total_Value_Of_Product_Sold");

						// all the variables are set between xml tags
						bw.write("\t\t<product_set> \n");
						bw.write("\t\t\t<product_line_name>" + ProductLine + "</product_line_name>\n");
						bw.write("\t\t\t<product>\n");
						bw.write("\t\t\t\t<product_name>" + ProductName + "</product_name>\n");
						bw.write("\t\t\t\t<product_vendor>" + ProductVendor + "</product_vendor>\n");
						bw.write("\t\t\t\t<units_sold>" + Unit_Sold + "</units_sold>\n");
						bw.write("\t\t\t\t<total_sales>" + Total_Value_Of_Product_Sold + "</total_sales>\n");
						bw.write("\t\t\t</product>\n");
						bw.write("\t\t</product_set> \n");
					
					}
					bw.write("\t</product_list>\n");
					bw.flush();
				
					/*  sql3 will select first name, last name, city, active customers and total order value 
					 * 	Inner query will calculate the sum of multiplication of priceEach and qunatityOrdered
					 *  Outer query will count the active customers
					 */ 
					String sql3 = "\r\n"
							+ "select sub.firstName, sub.lastName, sub.city, count(DISTINCT sub.Active_Customers) as "
							+ "Active_Customers, sum(sub.mul)\r\n"
							+ "as Total_Order_Value from\r\n"
							+ "(select employees.employeeNumber, count(customers.customerNumber) as Active_Customers, "
							+ "offices.city, customers.customerNumber, employees.firstName,\r\n"
							+ "employees.lastName, sum(orderdetails.priceEach * orderdetails.quantityOrdered) as mul  from "
							+ "employees \r\n"
							+ "inner join customers on employees.employeeNumber= customers.salesRepEmployeeNumber inner join \r\n"
							+ "orders on orders.customerNumber = customers.customerNumber inner join orderdetails \r\n"
							+ "on orderdetails.orderNumber = orders.orderNumber inner join offices \r\n"
							+ "on offices.officeCode = employees.officeCode\r\n" + "where orders.orderDate between '"
							+ sDate + "' and '" + eDate + "' group by customerNumber) as sub group by employeeNumber;";

					// ResultSet will hold all the data retrieved
					ResultSet rs3 = st.executeQuery(sql3);

					// below is the code to print the xml tag in xml file
					bw.write("\t<staff_list>\n");
					while (rs3.next()) {

						// all the values that are retrieved from resultset are stored in string
						String firstName = rs3.getString("firstName");
						String lastName = rs3.getString("lastName");
						String city = rs3.getString("city");
						String Active_Customers = rs3.getString("Active_Customers");
						String Total_Order_Value = rs3.getString("Total_Order_Value");

						// all the variables are set between xml tags
						bw.write("\t\t<employee> \n");
						bw.write("\t\t\t<first_name>" + firstName + "</first_name>\n");
						bw.write("\t\t\t<last_name>" + lastName + "</last_name>\n");
						bw.write("\t\t\t<office_city>" + city + "</office_city>\n");
						bw.write("\t\t\t<active_customers>" + Active_Customers + "</active_customers>\n");
						bw.write("\t\t\t<total_sales>" + Total_Order_Value + "</total_sales>\n");
						bw.write("\t\t</employee> \n");
	
					}
					bw.write("\t</staff_list>\n");

					bw.write("</year_end_summary>\n");

					bw.flush();
					System.out.println("XML file has been successfully created ");

					/* Closing all the result set statements and connections
					 */
					rs3.close();
					rs2.close();
					rs1.close();
					st.close();
					con.close();
					break;

				case "2":
					System.out.println("You pressed 2 to exit");
					System.exit(0);
					break;
				default:
					System.out.println("You have entered wrong input ");
					break;
				
						

				}
			

			}

			catch (Exception e) {
				System.out.println("Re-Enter");
			}
		}

	}

}
