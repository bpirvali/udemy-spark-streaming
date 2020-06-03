package p1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {
  val aBoolean: Boolean = false
  val aUnit: Unit = println("Something")

  def myFun(x: Int) = 43

  // -------------------
  // -- OOP
  // -------------------
  class Animal

  class Cat extends Animal

  trait Carnivore {
    def eat(aAnimal: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(aAnimal: Animal): Unit = println("Crunch!")
  }

  // companions: can see private members, etc...
  object Carnivore {
  }

  // generics
  trait MyList[Animal]

  val x = 1 + 2
  val y = 1.+(2)
  // -------------------
  // -- OOP
  // -------------------

  // ---------------------------
  // -- Functional Programming
  // ---------------------------
  // Functions are instance of a trait:
  // Function1, Function2, .... Function22
  val incrementer1: Function1[Int, Int] = new Function[Int, Int] {
    override def apply(x: Int): Int = x + 1
  }
  val incrementer2: Int => Int = new (Int => Int) {
    override def apply(x: Int): Int = x + 1
  }
  val incrementer3: Int => Int = (x: Int) => x + 1
  val incrementer4: Int => Int = x => x + 1

  val fun1: (Int, Int) => Int = (x: Int, y: Int) => x + y
  val fun2: (Int, Int) => Int = (x, y) => x + y

  // using
  val incremented = incrementer1(42)
  val incrementedList1 = List(1, 2, 3).map(incrementer1)

  // higher order functions: map, flatMap, filter ... Functions, getting other functions as args
  val incrementedList2 = List(1, 2, 3).map(x => x + 1)

  // pattern matching
  // good for de-composing structures and binding its parts to some names
  val unknown: Any = 45
  val ordinal = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }
  try {
    throw new NullPointerException
  } catch {
    case _: NullPointerException => "null pointer"
    case _ => "something else"
  }

  // Future: computations on separate threads

  // - imported scope implicit
  // implicit lazy val global: ExecutionContext = impl.ExecutionContextImpl.fromExecutor(null: Executor)
  import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture = Future {
    // some computations on another thread
    42
  }
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"I've found meaning of life:$meaningOfLife")
    case Failure(_) => println("Still searching on the meaning of life!")
  }

  // Partial Functions
  val aPartialFunction1 = (x: Int) => x match {
    case 1 => 43
    case 8 => 56
  }
  // ==>
  val aPartialFunction2: PartialFunction[Int, Int] =  {
    case 1 => 43
    case 8 => 56
  }

  // Implicits
  // - auto injection by compiler
  def methodWithImplicitArgument(implicit x: Int) = x + 43
  implicit val aImplicitInt = 67
  val implicitCall = methodWithImplicitArgument

  // case class
  // ... classes that have a bunch of utility methods implemented by the compiler
  case class Person(name: String) {
    def greet = println(s"Hi, my name is $name")
  }
  // implicit def:
  implicit def fromStringToPerson(name: String) = Person(name)
  "Bob".greet

  // implicit class (preferred to implicit defs)
  implicit class Dog(name: String) {
    def bark = println("Bark!")
  }
  "JC".bark
  /*
    - 1) local scope
    - 2) imported scope
    - 3) companion objects of the types involved in the method call
  */
  // 3) companion objects of the types involved in the method call
  // compiler is looking for a "Ordering[B]" either in the companion object of "List" or
  // in the companion object of "Int"
  List(1,2,3).sorted
  //val x: Int = 1
}