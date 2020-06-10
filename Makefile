CS3524_HOME="$(HOME)/CS3524"
RM_FLAGS="-f"

mud:
	javac cs3524/solutions/mud/Edge.java; \
	javac cs3524/solutions/mud/Vertex.java; \
	javac cs3524/solutions/mud/MUD.java; \
	javac cs3524/solutions/mud/MUDClientImpl.java; \
	javac cs3524/solutions/mud/MUDClientMainline.java; \
	javac cs3524/solutions/mud/MUDServerImpl.java; \
	javac cs3524/solutions/mud/MUDServerMainline.java; 

cleanmud:
	cd cs3524/solutions/mud; \
	rm $(RM_FLAGS) *.class *~; \
	cd $(CS3524_HOME);

