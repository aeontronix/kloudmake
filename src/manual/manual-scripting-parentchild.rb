sshresource = AeonBuild.create( "core.ssh", "serverid", { address => "server.domain" } )

AeonBuild.create( "core.file", "fileid", { path => "/tmp/test.txt", content => "Hello World" }, sshresource )
AeonBuild.create( "core.package", attrs={ name = "mysql" }, parent=sshresource )
$strm.createResource( "core.package", "web", sshresource ).set( { name => "nginx" } )
