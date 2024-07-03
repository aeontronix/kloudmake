include AeonBuild

class MyAction < Task
  def initialize
    super
    stagePrepare
    order 1
  end

  def execute(context, resource)
    resource.set({"key3" => "val3", "key4" => "val4"})
  end
end

AeonBuild.create("test.test", "myres", {"key1" => "val1", "key2" => "val2"}).addTask(MyAction.new())
