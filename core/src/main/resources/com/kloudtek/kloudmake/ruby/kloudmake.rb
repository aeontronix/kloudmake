require "java"
java_import com.kloudtek.kloudmake.AbstractTask

module Kloudmake
  class Task < com.kloudtek.kloudmake.AbstractTask

  end

  def Kloudmake.create(type, id=nil, attrs=nil, parent=nil)
    $kmrm.create(type, id, attrs, parent)
  end
end
