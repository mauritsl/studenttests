package nl.ou.edwards.actors;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import akka.actor.AbstractActor;

import nl.ou.edwards.*;

/**
 * Write results to Excel sheet.
 */
public class WriteExcelSheet extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output { }
  
  @Override
  public Receive createReceive() {
    
    return receiveBuilder()
      .match(
        Input.class,
        input -> {
          String testDir = Config.getInstance().getTestDirectory();
          String filename = testDir + "/results.xlsx";
          
          // Create a new Excel file.
          Workbook workbook = new XSSFWorkbook();

          // Add matrixes.
          if (input.getFullMatrix() == null) {
            addMatrix(workbook, "Matrix", input.getMatrix());
          } else {
            addMatrix(workbook, "Deduplicated Matrix", input.getMatrix());
            addMatrix(workbook, "Full matrix", input.getFullMatrix());
          }
          
          // Add implementation and method sheets.
          addImplementations(workbook, "Implementations", input);
          addMethods(workbook, "Unique methods", input.getMatrix().getTestMethods());
          
          // Write Excel file to disk.
          FileOutputStream fileOut = new FileOutputStream(filename);
          workbook.write(fileOut);
          fileOut.close();
          workbook.close();
          System.out.println("Written to " + filename);
          
          // Notify sender that we're done.
          getSender().tell(new Output(), getSelf());
        }
      )
      .build();
  }
  
  /**
   * Write Matrix to Excel sheet.
   */
  private void addMatrix(Workbook workbook, String sheetName, Matrix matrix) {
    Sheet sheet = workbook.createSheet(sheetName);
    sheet.createFreezePane(3, 1);
    
    // Write header row with implementation names.
    Row headerRow = sheet.createRow(0);
    int columnNum = 2;
    
    headerRow.createCell(0).setCellValue("Implementation");
    headerRow.createCell(1).setCellValue("Method");
    headerRow.createCell(2).setCellValue("Variant");
    
    for (Implementation impl : matrix.getImplementations()) {
      Cell cell = headerRow.createCell(++columnNum);
      cell.setCellValue(impl.getName());
    }
    
    List<Method> testMethods = matrix.getTestMethods();
    for (int i = 0; i < testMethods.size(); ++i) {
      Row row = sheet.createRow(i + 1);
      
      Method method = testMethods.get(i);
      row.createCell(0).setCellValue(method.getSource().getName());
      row.createCell(1).setCellValue(method.getName());
      row.createCell(2).setCellValue(method.getVariant());
      
      // Write results for this test method.
      for (int j = 0; j < matrix.getImplementations().size(); ++j) {
        row.createCell(j + 3).setCellValue(matrix.getPass(j, i) ? 1 : 0);
      }
    }
  }
  
  /**
   * List implementations in Excel sheet.
   */
  private void addImplementations(Workbook workbook, String sheetName, Results results) {
    Sheet sheet = workbook.createSheet(sheetName);
    sheet.createFreezePane(1, 1);
    
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Name");
    headerRow.createCell(1).setCellValue("Class LOC");
    headerRow.createCell(2).setCellValue("Test LOC");
    headerRow.createCell(3).setCellValue("Test methods");
    headerRow.createCell(4).setCellValue("BRC");
    
    List<String> implementationNames = results.getImplementationNames();
    for (int i = 0; i < implementationNames.size(); ++i) {
      String name = implementationNames.get(i);
      Implementation impl = results.getImplementation(name);
      
      Row row = sheet.createRow(i + 1);
      row.createCell(0).setCellValue(name);
      row.createCell(1).setCellValue(impl.getClassLoc());
      row.createCell(2).setCellValue(impl.getTestLoc());
      row.createCell(3).setCellValue(impl.getTestMethods().size());
      row.createCell(4).setCellValue(impl.getBugRevealingCapacity());
    }
    
  }
  
  /**
   * List methods in Excel sheet.
   */
  private void addMethods(Workbook workbook, String sheetName, List<Method> methods) {
    Sheet sheet = workbook.createSheet(sheetName);
    sheet.createFreezePane(3, 1);
    
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Implementation");
    headerRow.createCell(1).setCellValue("Name");
    headerRow.createCell(2).setCellValue("Variant");
    headerRow.createCell(3).setCellValue("Duplicates");
    headerRow.createCell(4).setCellValue("Similar");
    headerRow.createCell(7).setCellValue("Similarity");

    for (int i = 0; i < methods.size(); ++i) {
      Method method = methods.get(i);
      Row row = sheet.createRow(i + 1);
      row.createCell(0).setCellValue(method.getSource().getName());
      row.createCell(1).setCellValue(method.getName());
      row.createCell(2).setCellValue(method.getVariant());
      row.createCell(3).setCellValue(method.getDuplicates().size());
      Method similar = method.getSimilarMethod();
      if (similar != null) {
        row.createCell(4).setCellValue(similar.getSource().getName());
        row.createCell(5).setCellValue(similar.getName());
        row.createCell(6).setCellValue(similar.getVariant());
        row.createCell(7).setCellValue(method.getSimilarity());
      }
    }
    
  }
}
