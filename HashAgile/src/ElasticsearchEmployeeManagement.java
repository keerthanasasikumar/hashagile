import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Employee {
    String id;
    String name;
    String department;
    String gender;

    public Employee(String id, String name, String department, String gender) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Department: " + department + ", Gender: " + gender;
    }
}

public class ElasticsearchEmployeeManagement {
    static Map<String, List<Employee>> collections = new HashMap<>();

    public static void createCollection(String collectionName) {
        collections.putIfAbsent(collectionName, new ArrayList<>());
        System.out.println("Collection " + collectionName + " created.");
    }

    public static void indexData(String collectionName, String excludeColumn) {
        List<Employee> employeeList = new ArrayList<>();
        // Use the absolute file path if necessary
        File file = new File("Employee Sample Data 1.csv");

        if (!file.exists()) {
            System.out.println("Error: File 'Employee Sample Data 1.csv' not found.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String id = data[0].trim();
                String name = data[1].trim();
                String department = excludeColumn.equals("Department") ? null : data[2].trim();
                String gender = excludeColumn.equals("Gender") ? null : data[3].trim();
                employeeList.add(new Employee(id, name, department, gender));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        collections.put(collectionName, employeeList);
        System.out.println("Data indexed into collection: " + collectionName);
    }

    public static void searchByColumn(String collectionName, String columnName, String columnValue) {
        List<Employee> employees = collections.getOrDefault(collectionName, new ArrayList<>());
        List<Employee> result = employees.stream()
                .filter(emp -> (columnName.equals("Department") && columnValue.equals(emp.department)) ||
                               (columnName.equals("Gender") && columnValue.equals(emp.gender)))
                .collect(Collectors.toList());
        System.out.println("Search results for " + columnName + " = " + columnValue + ":");
        result.forEach(System.out::println);
    }

    public static void getEmpCount(String collectionName) {
        List<Employee> employees = collections.getOrDefault(collectionName, new ArrayList<>());
        System.out.println("Employee count in " + collectionName + ": " + employees.size());
    }

    public static void delEmpById(String collectionName, String employeeId) {
        List<Employee> employees = collections.getOrDefault(collectionName, new ArrayList<>());
        employees.removeIf(emp -> emp.id.equals(employeeId));
        System.out.println("Employee with ID " + employeeId + " deleted from " + collectionName);
    }

    public static void getDepFacet(String collectionName) {
        List<Employee> employees = collections.getOrDefault(collectionName, new ArrayList<>());
        Map<String, Long> departmentCount = employees.stream()
                .filter(emp -> emp.department != null)
                .collect(Collectors.groupingBy(emp -> emp.department, Collectors.counting()));
        System.out.println("Department facet count in " + collectionName + ": " + departmentCount);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the name of the first collection:");
        String v_nameCollection = scanner.nextLine();
        System.out.println("Enter the phone of the second collection:");
        String v_phoneCollection = scanner.nextLine();

        createCollection(v_nameCollection);
        createCollection(v_phoneCollection);

        getEmpCount(v_nameCollection);

        System.out.println("Indexing data for " + v_nameCollection + ". Enter the column to exclude (Department/Gender):");
        String excludeColumn1 = scanner.nextLine();
        indexData(v_nameCollection, excludeColumn1);

        System.out.println("Indexing data for " + v_phoneCollection + ". Enter the column to exclude (Department/Gender):");
        String excludeColumn2 = scanner.nextLine();
        indexData(v_phoneCollection, excludeColumn2);

        getEmpCount(v_nameCollection);

        System.out.println("Enter the ID of the employee to delete from " + v_nameCollection + ":");
        String employeeId = scanner.nextLine();
        delEmpById(v_nameCollection, employeeId);
        getEmpCount(v_nameCollection);

        System.out.println("Search in " + v_nameCollection + ". Enter the column name to search by (Department/Gender):");
        String searchColumn1 = scanner.nextLine();
        System.out.println("Enter the value to search for:");
        String searchValue1 = scanner.nextLine();
        searchByColumn(v_nameCollection, searchColumn1, searchValue1);

        System.out.println("Search in " + v_phoneCollection + ". Enter the column name to search by (Department/Gender):");
        String searchColumn2 = scanner.nextLine();
        System.out.println("Enter the value to search for:");
        String searchValue2 = scanner.nextLine();
        searchByColumn(v_phoneCollection, searchColumn2, searchValue2);

        getDepFacet(v_nameCollection);
        getDepFacet(v_phoneCollection);

        scanner.close();
    }
}
