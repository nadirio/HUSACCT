package husaccttest.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import husacct.control.domain.Workspace;
import husacct.control.task.MainController;
import husacct.control.task.WorkspaceController;

import java.io.File;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WorkspaceControllerTest {

	WorkspaceController workspaceController;
	File testFile = new File("WorkspaceTestFile.xml");
	
	@Before
	public void setup(){
		MainController mainController = new MainController();
		workspaceController = mainController.getWorkspaceController();
	}
	
	@After
	public void cleanup(){
		testFile.delete();
	}
	
	@Test
	public void testInitialWorkspace(){
		assertNull(workspaceController.getCurrentWorkspace());
	}
	
	@Test
	public void testNewWorkspace(){
		workspaceController.createWorkspace("JUnitTestWorkspace");
		assertNotNull(workspaceController.getCurrentWorkspace());
	}
	
	@Test
	public void testIsOpenWorkspace(){
		workspaceController.createWorkspace("JUnitTestWorkspace");
		assertTrue(workspaceController.isOpenWorkspace());
	}
	
	@Test
	public void testCloseWorkspace(){
		workspaceController.createWorkspace("JUnitTestWorkspace");
		workspaceController.closeWorkspace();
		assertNull(workspaceController.getCurrentWorkspace());
	}
	
	@Test
	public void testSetWorkspace(){
		Workspace workspace1 = new Workspace();
		workspaceController.setWorkspace(workspace1);
		Workspace workspace2 = workspaceController.getCurrentWorkspace();
		assertSame(workspace1, workspace2);
	}
	
	@Test
	public void testGetWorkspaceData(){
		Document doc = workspaceController.getWorkspaceData();
		assertNotNull(doc.getRootElement());
	}
	
	@Test
	public void testLoadWorkspaceData(){
		Document doc1 = workspaceController.getWorkspaceData();
		workspaceController.loadWorkspace(doc1);
		Document doc2 = workspaceController.getWorkspaceData();
		XMLOutputter outputter = new XMLOutputter();
		String doc1String = outputter.outputString(doc1);
		String doc2String = outputter.outputString(doc2);
		assertEquals(doc1String.length(), doc2String.length());
	}
	
	@Test
	public void testSaveWorkspace(){
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("file", testFile);
		workspaceController.saveWorkspace("xml", data);
		assertTrue(testFile.exists());
	}
	
	@Test
	public void testLoadWorkspace(){
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("file", testFile);
		workspaceController.loadWorkspace("xml", data);
		assertNotNull(workspaceController.getCurrentWorkspace());
		
	}
}