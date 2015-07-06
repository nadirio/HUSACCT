package husacct.analyse.domain.famix;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import husacct.analyse.domain.IModelQueryService;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.common.dto.AnalysisStatisticsDTO;
import husacct.common.dto.DependencyDTO;

public class FamixQueryServiceImpl implements IModelQueryService {

    private FamixModel theModel;
    private FamixModuleFinder moduleFinder;
    private FamixDependencyFinder dependencyFinder;

    public FamixQueryServiceImpl() {
        this.theModel = FamixModel.getInstance();
        clearModel();
        this.moduleFinder = new FamixModuleFinder(theModel);
        this.dependencyFinder = new FamixDependencyFinder(theModel);
    }
    
    @Override
    public void clearModel() {
    	theModel.clear();
        this.dependencyFinder = new FamixDependencyFinder(theModel);
    }

    @Override
    public void buildCache(){
    	dependencyFinder.buildCache();
    }

    @Override
    public void importDependencies(List<DependencyDTO> dependencies){
    	dependencyFinder.importDependencies(dependencies);
    }

    @Override
    public SoftwareUnitDTO getSoftwareUnitByUniqueName(String uniqueName) {
        return moduleFinder.getModuleForUniqueName(uniqueName);
    }

    @Override
    public String getSourceFilePathOfClass(String uniqueName) {
    	String returnValue = "";
    	if (theModel.classes.containsKey(uniqueName)) {
    		returnValue = theModel.classes.get(uniqueName).sourceFilePath;
    	}
    	return returnValue;
    }

    @Override
    public SoftwareUnitDTO[] getSoftwareUnitsInRoot() {
    	List<SoftwareUnitDTO> rootModules = moduleFinder.getRootModules();
        return rootModules.toArray(new SoftwareUnitDTO[rootModules.size()]);
    }

    @Override
    public SoftwareUnitDTO[] getChildUnitsOfSoftwareUnit(String uniqueName) {
    	SoftwareUnitDTO[] children;
    	List<SoftwareUnitDTO> childModules = moduleFinder.getChildModulesInModule(uniqueName);
        int numberOfChildren = childModules.size();
        if ((childModules != null) && numberOfChildren > 0) {
	        children = childModules.toArray(new SoftwareUnitDTO[childModules.size()]);
        } else {
        	children = new SoftwareUnitDTO[0];
        }
	    return children;
    }

    @Override
    public SoftwareUnitDTO getParentUnitOfSoftwareUnit(String uniqueName) {
        return moduleFinder.getParentModuleForModule(uniqueName);
    }

    @Override
    public DependencyDTO[] getAllDependencies() {
        List<DependencyDTO> result = dependencyFinder.getAllDependencies();
        DependencyDTO[] allDependencies = result.toArray(new DependencyDTO[result.size()]);
        return allDependencies;
    }

    @Override
    public DependencyDTO[] getDependenciesFromSoftwareUnitToSoftwareUnit(String pathFrom, String pathTo) {
        List<DependencyDTO> foundDependenciesReturnList = new ArrayList<DependencyDTO>();
	    TreeMap<String, DependencyDTO> foundDependenciesTreeMap = new TreeMap<String, DependencyDTO>();
    	TreeSet<String> allFromTypeNames = getPhysicalClassPathsOfSoftwareUnit(pathFrom);
    	TreeSet<String> allToTypeNames = getPhysicalClassPathsOfSoftwareUnit(pathTo);
        for (String fromTypeName : allFromTypeNames) {
            for (String toTypeName : allToTypeNames) {
                for (DependencyDTO dependency : dependencyFinder.getDependenciesFromTo(fromTypeName, toTypeName)) {
					// Filter-out duplicate dependencies
					String uniqueName = (dependency.from + dependency.to + dependency.lineNumber + dependency.type + dependency.subType + Boolean.toString(dependency.isIndirect));
					if (!foundDependenciesTreeMap.containsKey(uniqueName)){
						foundDependenciesTreeMap.put(uniqueName, dependency);
					}
                }
            }
        }
        foundDependenciesReturnList.addAll(foundDependenciesTreeMap.values());
        return foundDependenciesReturnList.toArray(new DependencyDTO[foundDependenciesReturnList.size()]);
    }
    
    @Override
	public DependencyDTO[] getDependenciesFromClassToClass(String classPathFrom, String classPathTo){
    	ArrayList<DependencyDTO> result = dependencyFinder.getDependenciesFromTo(classPathFrom, classPathTo);
        DependencyDTO[] allDependencies = result.toArray(new DependencyDTO[result.size()]);
        return allDependencies;
	}
	
    // Returns List with unique names of all types (classes, interfaces, inner classes) within the SoftwareUnit with uniqueName  
    @Override
    public List<String> getAllPhysicalClassPathsOfSoftwareUnit(String uniqueName){
    	List<String> returnValue = new ArrayList<String>(getPhysicalClassPathsOfSoftwareUnit(uniqueName)); 
    	return returnValue;
    }
    
    // Returns TreeSet with unique names of all types (classes, interfaces, inner classes) within the SoftwareUnit with uniqueName  
    private TreeSet<String> getPhysicalClassPathsOfSoftwareUnit(String uniqueName){
		TreeSet<String> uniqueNamesAllFoundTypes = new TreeSet<String>();
		if (!theModel.packages.containsKey(uniqueName)) { // Add only classes and libraries
			uniqueNamesAllFoundTypes.add(uniqueName);
		}
		TreeSet<String> children = (moduleFinder.getChildModulesNamesInModule(uniqueName));
    	if ((children != null) && (children.size() > 0)){
	    	for (String child : children){
	    		TreeSet<String> validChildName = getPhysicalClassPathsOfSoftwareUnit(child);
	    		uniqueNamesAllFoundTypes.addAll(validChildName);
	    	}
    	}
		return uniqueNamesAllFoundTypes;
    }

    // Returns List with unique names of all packages within this SoftwareUnit  
    @Override
    public List<String> getAllPhysicalPackagePathsOfSoftwareUnit(String uniqueName){
    	List<String> returnValue = new ArrayList<String>(getPhysicalPackagePathsOfSoftwareUnit(uniqueName)); 
    	return returnValue;
    }
    
    // Returns TreeSet with unique names of all packages within this SoftwareUnit  
    private TreeSet<String> getPhysicalPackagePathsOfSoftwareUnit(String uniqueName){
		TreeSet<String> uniqueNamesAllFoundPackages = new TreeSet<String>();
		if (theModel.packages.containsKey(uniqueName)) { // Add only packages
			//uniqueNamesAllFoundPackages.add(uniqueName);
		}
		TreeSet<String> children = (moduleFinder.getChildModulesNamesInModule(uniqueName));
    	if ((children != null) && (children.size() > 0)){
	    	for (String child : children){
	    		if (theModel.packages.containsKey(child)) { // Add only packages
	    			uniqueNamesAllFoundPackages.add(child);
	    		}
	    		TreeSet<String> validChildName = getPhysicalPackagePathsOfSoftwareUnit(child);
	    		uniqueNamesAllFoundPackages.addAll(validChildName);
	    	}
    	}
		return uniqueNamesAllFoundPackages;
    }

    @Override
    // If selectedModule == null, statistics of the whole application are returned; otherwise statistics of the selectedModule only are returned. 
	public AnalysisStatisticsDTO getAnalysisStatistics(SoftwareUnitDTO selectedModule) {
		AnalysisStatisticsDTO returnValue;
		// Determine totalNrOfPackages, minus 1 for package xLibraries, since that one is created within the analysis process. 
		int totalNrOfPackages = 0;
		totalNrOfPackages = theModel.packages.size();
		if (theModel.packages.containsKey("xLibraries")) {
			totalNrOfPackages --;
		}
		if (selectedModule == null) {
			returnValue = new AnalysisStatisticsDTO(totalNrOfPackages, theModel.classes.size(), theModel.getTotalNumberOfLinesOfCode(), getAllDependencies().length, 0, 0, 0);
		} else {
			int packages = 0;
			int classes = 0;
			int linesOfCode = 0;
			if (selectedModule.type.equals("package")) {
				if (theModel.packages.containsKey(selectedModule.uniqueName)) {
					packages = getPhysicalPackagePathsOfSoftwareUnit(selectedModule.uniqueName).size();
					TreeSet<String> classesSet = getPhysicalClassPathsOfSoftwareUnit(selectedModule.uniqueName);
					classes = classesSet.size();
					for (String typeName : classesSet) {
						if (theModel.classes.containsKey(typeName)) {
							FamixClass selected = theModel.classes.get(typeName);
							linesOfCode = linesOfCode + selected.linesOfCode;
						}
					}
				}
			} else if (selectedModule.type.equals("class") || selectedModule.type.equals("interface")) {
				if (theModel.classes.containsKey(selectedModule.uniqueName)) {
					FamixClass selected = theModel.classes.get(selectedModule.uniqueName);
					linesOfCode = selected.linesOfCode;
					if (selected.hasInnerClasses) {
						TreeSet<String> classesSet = getPhysicalClassPathsOfSoftwareUnit(selectedModule.uniqueName);
						classes = classesSet.size();
					} else {
						classes = 1;
					}
				}
				packages = 0;
			} else {
				// A library is selected, so return default 0-values.
			}
			returnValue = new AnalysisStatisticsDTO(totalNrOfPackages, theModel.classes.size(), theModel.getTotalNumberOfLinesOfCode(), getAllDependencies().length, packages, classes, linesOfCode);
		}
        return returnValue;
    }

}
