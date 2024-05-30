{ nixpkgs ? import <nixpkgs> {}, jdkVersion ? "8" }:

with nixpkgs;

let 
  jdk8 = pkgs.jdk8;
  jdk17 = pkgs.jdk17;

in
pkgs.stdenv.mkDerivation rec {
  name = "nix-shell";
  buildInputs = [
    nodejs_21
    jdk8
    jdk17 
    sqlitebrowser
  ];

  JAVA_HOME_8 = "${jdk8}";
  JAVA_HOME_17 = "${jdk17}";
  EXPLICIT_JDK_VERSION = "${jdkVersion}";

  shellHook = ''
    if [ "$EXPLICIT_JDK_VERSION" = "17" ]; then
      echo "Set JAVA_HOME=jdk17"
      export JAVA_HOME="$JAVA_HOME_17"
    elif [ "$EXPLICIT_JDK_VERSION" = "8" ]; then
      echo "Set JAVA_HOME=jdk8"
      export JAVA_HOME="$JAVA_HOME_8"
    elif [ -n "$EXPLICIT_JDK_VERSION" ]; then
      echo "Unknown value for jdkVersion, Set JAVA_HOME=jdk8"
      export JAVA_HOME="$JAVA_HOME_8"
    fi

    export PATH="$PWD/scripts:$PATH"
    export PATH=$JAVA_HOME/bin:$PATH
    export PROJECT_DIR="$PWD"

    chmod +x $PWD/scripts/*

    export M2_LOCAL_REPOSITORY=$(mvnw help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)
  '';
}
