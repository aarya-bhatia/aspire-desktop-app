Compile
javac --module-path $PATH_TO_FX -d mods/Aspire $(find src -name "*.java")

Run
java --module-path $PATH_TO_FX:mods -m Aspire/app.CPTPlotter

Build
$JAVA_HOME/bin/jlink --module-path $PATH_TO_FX_MODS:mods --add-modules Aspire --output Aspire

Package
$JAVA_HOME/bin/jpackage -n Aspire --runtime-image Aspire -m Aspire/app.CPTPlotter