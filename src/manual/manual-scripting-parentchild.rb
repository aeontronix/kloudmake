sshresource = SysTyrant.create( "core.ssh", "serverid", { address => "server.domain" } )

SysTyrant.create( "core.file", "fileid", { path => "/tmp/test.txt", content => "Hello World" }, sshresource )
SysTyrant.create( "core.package", attrs={ name = "mysql" }, parent=sshresource )
$strm.createResource( "core.package", "web", sshresource ).set( { name => "nginx" } )
