This is an OpenDJ authentication policy plugin for users whose credentials
are managed by an external Kerberos realm.

Requirements
------------
  1. Java sdk 1.7 or above
  2. maven 3.0
  3. Sources of OpenDJ

Prepare Build environment
-------------------------
Unpack OpenDJ source and run install
```
$ mkdir tmp
$ cd tmp
$ unzip <path/to/OpenDJ-3.5.x-src.zip>
$ mvn clean install
```

Build and Install
----------------
  1. build and install the extention
    ```
    $ mvn clean package
    ```
    
  2. add opendj-kpa to your OpenDJ installation
    ```
    $ cd <opendj-install directory>
    $ unzip opendj-kpa-xxx.zip
    ```
    
  3. restart the server
    ```
    $ bin/stop-ds --restart
    ```
    
  4. configure the pass-through for kerberos
    ```
    $ bin/dsconfig -X create-password-policy \
       --type kerberos-pass-through \
       --policy-name "Krb5 Pass Through" \
       --set krb5-realm:EXAMPLE.COM \
       --set mapped-attribute:uid
      ```
      
  5. assign pass-through authentication to users

    You assign authentication policies in the same way as you assign password
    policies, by using the ***ds-pwp-password-policy-dn*** attribute:
    ```
    ds-pwp-password-policy-dn: cn=Krb5 Pass Through,cn=Password Policies,cn=config
    ```

    Users depending on pass through authentication no longer need a local password policy,
    as they no longer authenticate locally.

More Info
---------
[Configuring Pass Through Authentication][1]

  [1]: https://backstage.forgerock.com/docs/opendj/3.5/admin-guide/chap-pwd-policy
