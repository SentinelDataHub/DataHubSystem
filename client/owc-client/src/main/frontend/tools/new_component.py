import ConfigParser, os
import shutil
from tempfile import mkstemp
from shutil import move
from os import remove, close
import sys


def main():


	functions = {
    	'create': create
	}
	print str(sys.argv[1])
	try:
		func = functions[str(sys.argv[1])]
		func()		
	except:
		print "Invalid argument! possible entries: [help | create]."



def create():
	config = ConfigParser.ConfigParser()
	config.read("./tools/config.ini")
	projectPath = raw_input("Repository path (empty to load the path from configuration file): ")
	projectRepositoryUrl = raw_input("Repository url (empty to load the path from configuration file): ")
	if projectPath == '':
		projectPath = config.get("Main","RepositoryPath")
	if projectRepositoryUrl == '':
		projectRepositoryUrl = config.get("Main","RepositoryUrl")
	elementName = raw_input("New element name: ")
	elementClass = raw_input("New element class: ")
	elementDescription = raw_input("New element description: ")
	templatePath = str(projectPath) + "/app/elements/_template-element"
	newComponentPath = str(projectPath) + "/app/elements/" + str(elementName)
	print("Template path: " + str(templatePath) + ", new component path: " + str(newComponentPath) )
	createDirectory(newComponentPath)
	copytree(templatePath,newComponentPath)
	setDemo(newComponentPath, elementName)
	setTest(newComponentPath, elementName)
	setREADME(newComponentPath, elementName, elementDescription,  elementClass)
	setBower(newComponentPath, elementName, elementDescription, projectRepositoryUrl, elementClass)
	setWctFiles(newComponentPath, elementName)
	setElementFile(newComponentPath, elementName, elementDescription, elementClass)
	print("[DONE]")
	


def setDemo(newComponentPath,elementName):
	print "setting demo..."
	demoPath = str(newComponentPath) + "/demo"
	indexPath = str(demoPath) + "/index.html"
	replace(indexPath,"$$element_name$$",elementName)

def replace(file_path, pattern, subst):
    fh, abs_path = mkstemp()
    with open(abs_path,'w') as new_file:
        with open(file_path) as old_file:
            for line in old_file:
                new_file.write(line.replace(pattern, subst))
    close(fh)
    remove(file_path)
    move(abs_path, file_path)


def setTest(newComponentPath,elementName):
	print "setting test..."
	testPath = str(newComponentPath) + "/test"
	indexPath = str(testPath) + "/index.html"
	testTemplatePath = str(testPath) + "/template-element.html"
	componentTestPath = str(testPath) + "/" +str(elementName) +".html"
	replace(indexPath,"$$element_name$$",elementName)
	replace(testTemplatePath,"$$element_name$$",elementName)
	os.rename(testTemplatePath, componentTestPath)

def setREADME(newComponentPath,elementName, elementDescription,  elementClass):
	print "setting README..."
	readmePath = str(newComponentPath) + "/README.md"
	replace(readmePath,"$$element_name$$",elementName)
	replace(readmePath,"$$element_description$$",elementDescription)
	replace(readmePath,"$$element_class$$",elementClass)

def setBower(newComponentPath, elementName, elementDescription,projectRepositoryUrl, elementClass):
	print "setting bower.json..."
	bowerPath = str(newComponentPath) + "/bower.json"
	replace(bowerPath,"$$element_name$$",elementName)
	replace(bowerPath,"$$element_description$$",elementDescription)
	replace(bowerPath,"$$repository_url$$",projectRepositoryUrl)
	replace(bowerPath,"$$element_class$$",elementClass)



def setWctFiles(newComponentPath, elementName):
	print "setting wct files..."
	wctJsonPath = str(newComponentPath) + "/wct.conf.json"
	replace(wctJsonPath,"$$element_name$$",elementName)
	

def setElementFile(newComponentPath, elementName, elementDescription, elementClass):
	print "setting element file"
	elementPath = str(newComponentPath) + "/template-element.html"
	replace(elementPath,"$$element_name$$",elementName)
	replace(elementPath,"$$element_description$$",elementDescription)
	replace(elementPath,"$$element_class$$", elementClass)
	newNameComponentPath = str(newComponentPath) + "/"+str(elementName)+".html"
	os.rename(elementPath, newNameComponentPath)
	


def createDirectory(path):
	if not os.path.exists(path):
		os.makedirs(path)

def copytree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            shutil.copytree(s, d, symlinks, ignore)
        else:
            shutil.copy2(s, d)


if __name__ == "__main__": main()            