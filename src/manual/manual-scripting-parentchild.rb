sshresource = Kloudmake.create( "core.ssh", "serverid", { address => "server.domain" } )

Kloudmake.create( "core.file", "fileid", { path => "/tmp/test.txt", content => "Hello World" }, sshresource )
Kloudmake.create( "core.package", attrs={ name = "mysql" }, parent=sshresource )
$strm.createResource( "core.package", "web", sshresource ).set( { name => "nginx" } )
