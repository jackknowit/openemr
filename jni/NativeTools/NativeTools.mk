##
## Auto Generated makefile by CodeLite IDE
## any manual changes will be erased      
##
## Debug
ProjectName            :=NativeTools
ConfigurationName      :=Debug
WorkspaceConfiguration :=Debug
WorkspacePath          :=/home/jack/proj/oss/emr/jni
ProjectPath            :=/home/jack/proj/oss/emr/jni/NativeTools
IntermediateDirectory  :=$(ConfigurationName)
OutDir                 := $(IntermediateDirectory)
CurrentFileName        :=
CurrentFilePath        :=
CurrentFileFullPath    :=
User                   :=jack
Date                   :=03/22/23
CodeLitePath           :=/home/jack/.codelite
LinkerName             :=/usr/bin/g++
SharedObjectLinkerName :=/usr/bin/g++ -shared -fPIC
ObjectSuffix           :=.o
DependSuffix           :=.o.d
PreprocessSuffix       :=.i
DebugSwitch            :=-g 
IncludeSwitch          :=-I
LibrarySwitch          :=-l
OutputSwitch           :=-o 
LibraryPathSwitch      :=-L
PreprocessorSwitch     :=-D
SourceSwitch           :=-c 
OutputDirectory        :=$(IntermediateDirectory)
OutputFile             :=$(IntermediateDirectory)/lib$(ProjectName).so
Preprocessors          :=
ObjectSwitch           :=-o 
ArchiveOutputSwitch    := 
PreprocessOnlySwitch   :=-E
ObjectsFileList        :="NativeTools.txt"
PCHCompileFlags        :=
MakeDirCommand         :=mkdir -p
LinkOptions            :=  `pkg-config --libs gtk+-2.0`
IncludePath            :=  $(IncludeSwitch). $(IncludeSwitch). $(IncludeSwitch)/usr/lib/jvm/default-java/include $(IncludeSwitch)/usr/lib/jvm/default-java/include/linux $(IncludeSwitch)/usr/include/gtk-2.0 
IncludePCH             := 
RcIncludePath          := 
Libs                   := $(LibrarySwitch)X11 
ArLibs                 :=  "X11" 
LibPath                := $(LibraryPathSwitch). $(LibraryPathSwitch)/usr/lib/jvm/default-java//lib 

##
## Common variables
## AR, CXX, CC, AS, CXXFLAGS and CFLAGS can be overridden using an environment variable
##
AR       := /usr/bin/ar rcu
CXX      := /usr/bin/g++
CC       := /usr/bin/gcc
CXXFLAGS :=  -g -fPIC `pkg-config --cflags gtk+-2.0` $(Preprocessors)
CFLAGS   :=  -g -fPIC `pkg-config --cflags gtk+-2.0` $(Preprocessors)
ASFLAGS  := 
AS       := /usr/bin/as


##
## User defined environment variables
##
CodeLiteDir:=/usr/share/codelite
Objects0=$(IntermediateDirectory)/NativeTools.cpp$(ObjectSuffix) 



Objects=$(Objects0) 

##
## Main Build Targets 
##
.PHONY: all clean PreBuild PrePreBuild PostBuild MakeIntermediateDirs
all: $(OutputFile)

$(OutputFile): $(IntermediateDirectory)/.d $(Objects) 
	@$(MakeDirCommand) $(@D)
	@echo "" > $(IntermediateDirectory)/.d
	@echo $(Objects0)  > $(ObjectsFileList)
	$(SharedObjectLinkerName) $(OutputSwitch)$(OutputFile) @$(ObjectsFileList) $(LibPath) $(Libs) $(LinkOptions)
	@$(MakeDirCommand) "/home/jack/proj/oss/emr/jni/.build-debug"
	@echo rebuilt > "/home/jack/proj/oss/emr/jni/.build-debug/NativeTools"

MakeIntermediateDirs:
	@test -d $(ConfigurationName) || $(MakeDirCommand) $(ConfigurationName)


$(IntermediateDirectory)/.d:
	@test -d $(ConfigurationName) || $(MakeDirCommand) $(ConfigurationName)

PreBuild:


##
## Objects
##
$(IntermediateDirectory)/NativeTools.cpp$(ObjectSuffix): NativeTools.cpp
	@$(CXX) $(CXXFLAGS) $(IncludePCH) $(IncludePath) -MG -MP -MT$(IntermediateDirectory)/NativeTools.cpp$(ObjectSuffix) -MF$(IntermediateDirectory)/NativeTools.cpp$(DependSuffix) -MM NativeTools.cpp
	$(CXX) $(IncludePCH) $(SourceSwitch) "/home/jack/proj/oss/emr/jni/NativeTools/NativeTools.cpp" $(CXXFLAGS) $(ObjectSwitch)$(IntermediateDirectory)/NativeTools.cpp$(ObjectSuffix) $(IncludePath)
$(IntermediateDirectory)/NativeTools.cpp$(PreprocessSuffix): NativeTools.cpp
	$(CXX) $(CXXFLAGS) $(IncludePCH) $(IncludePath) $(PreprocessOnlySwitch) $(OutputSwitch) $(IntermediateDirectory)/NativeTools.cpp$(PreprocessSuffix) NativeTools.cpp


-include $(IntermediateDirectory)/*$(DependSuffix)
##
## Clean
##
clean:
	$(RM) -r $(ConfigurationName)/


