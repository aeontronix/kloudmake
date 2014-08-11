class MyAction < Action
  def initialize
    super
    type "PREPARE"
    order 1
  end

  def execute(context, resource)
    resource.set({"key3" => "val3", "key4" => "val4"})
  end
end

newres("test.test", "myres", {"key1" => "val1", "key2" => "val2"}).addAction(MyAction.new())
