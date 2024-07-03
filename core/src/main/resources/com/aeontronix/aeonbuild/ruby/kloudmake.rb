require "java"
java_import com.aeontronix.aeonbuild.AbstractTask

module AeonBuild
  class Task < com.aeontronix.aeonbuild.AbstractTask

  end

  def AeonBuild.create(type, id=nil, attrs=nil, parent=nil)
    $kmrm.create(type, id, attrs, parent)
  end
end
