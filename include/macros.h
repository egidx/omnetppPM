//==========================================================================
//  MACROS.H - part of
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//  Definition of the following macros:
//    Define_Network, Define_Link
//    ModuleInterface..End
//    Define_Module
//    Module_Class_Members
//    Define_Function
//    Register_Class
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2005 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __MACROS_H
#define __MACROS_H

#include "onstartup.h"
#include "carray.h"
#include "ctypes.h"

//=========================================================================

/**
 * @name Declaration macros
 * @ingroup Macros
 */
//@{

/**
 * Network declaration macro. It can be found in code generated by the
 * NEDC compiler. The use of this macro allows the creation of a network
 * when only its name is available as a string. (Typically, the name of the
 * network to be simulated is read from the configuration file.)
 * The macro expands to the definition of a cNetworkType object.
 *
 * @hideinitializer
 */
#define Define_Network(NAME) \
  EXECUTE_ON_STARTUP(NAME##__net, networks.instance()->add(new NAME(#NAME));)

/**
 * Registers a channel type definition (cChannelType object).
 *
 * @hideinitializer
 */
#define Define_Channel(NAME) \
  EXECUTE_ON_STARTUP(NAME##__channelt, channeltypes.instance()->add(new NAME(#NAME));)

/**
 * Registers a mathematical function that takes 0, 1, 2 or 3 double arguments
 * and returns a double. The use of this macro allows the function to be used
 * in expressions inside NED network descriptions.
 *
 * Commonly used <math.h> functions have Define_Function() lines in the \opp
 * simulation kernel.
 *
 * @hideinitializer
 */
#define Define_Function(NAME,ARGCOUNT) \
  EXECUTE_ON_STARTUP(NAME##__##ARGCOUNT##__func, functions.instance()->add(new cFunctionType(#NAME,NAME,ARGCOUNT));)

/**
 * Like Define_Function(), but takes three arguments, the second one being the
 * pointer to the function. This macro allows registering a function with a
 * different name than its implementation.
 *
 * @hideinitializer
 */
#define Define_Function2(NAME,FUNCTION,ARGCOUNT) \
  EXECUTE_ON_STARTUP(NAME##__##ARGCOUNT##__func, functions.instance()->add(new cFunctionType(#NAME,FUNCTION,ARGCOUNT));)

/**
 * Register class. This defines a factory object which makes it possible
 * to create an object by the passing class name to the createOne() function.
 * The class must be a subclass of cPolymorphic, otherwise a compile-time error
 * will occur: <i>"cannot convert..."</i>
 *
 * @hideinitializer
 */
#define Register_Class(CLASSNAME) \
  cPolymorphic *CLASSNAME##__create() {return new CLASSNAME;} \
  EXECUTE_ON_STARTUP(CLASSNAME##__class, classes.instance()->add(new cClassRegister(#CLASSNAME,CLASSNAME##__create));)

//@}

//=========================================================================

/**
 * @name Module declaration macros
 * @ingroup Macros
 */
//@{

/**
 * Announces the class as a module to \opp and couples it with the
 * NED interface of the same name. The macro expands to the definition
 * a cModuleType object.
 *
 * The NEDC compiler generates Define_Module() lines for all compound modules.
 * However, it is the user's responsibility to put Define_Module() lines for
 * all simple module types into one of the C++ sources.
 *
 * @hideinitializer
 */
#define Define_Module(CLASSNAME) \
  static cModule *CLASSNAME##__create() {return new CLASSNAME();} \
  EXECUTE_ON_STARTUP(CLASSNAME##__mod, modtypes.instance()->add(new cModuleType(#CLASSNAME,#CLASSNAME,(ModuleCreateFunc)CLASSNAME##__create));)

/**
 * Similar to Define_Module(), except that it couples the class with the
 * NED interface of the given name.
 *
 * While this macro continues to be supported, it is NOT RECOMMENDED because
 * modules defined with it don't show up in documentation generated with
 * opp_neddoc. One can use NED's <tt>like</tt> feature with the normal
 * Define_Module() macro too, it doesn't require Define_Module_Like().
 *
 * @hideinitializer
 */
#define Define_Module_Like(CLASSNAME,INTERFACENAME) \
  static cModule *CLASSNAME##__create() {return new CLASSNAME();} \
  EXECUTE_ON_STARTUP(CLASSNAME##__mod, modtypes.instance()->add(new cModuleType(#CLASSNAME,#INTERFACENAME,(ModuleCreateFunc)CLASSNAME##__create));)

/**
 * This macro facilitates the declaration of a simple module class, and
 * it expands to the definition of mandatory member functions.
 * (Currently only a constructor.)
 *
 * The macro is used like this:
 *
 * <PRE>
 *  class CLASSNAME : public cSimpleModule
 *  {
 *     Module_Class_Members(CLASSNAME,cSimpleModule,8192)
 *     virtual void activity();
 *  };
 * </PRE>
 *
 * @hideinitializer
 */
// TODO dummy args can be removed in a later version, when all models have been ported
#define Module_Class_Members(CLASSNAME,BASECLASS,STACK) \
    public: \
      CLASSNAME(const char *dummy1=0, cModule *dummy2=0, unsigned stk=STACK) : BASECLASS(0,0,stk) {}
//@}

//=========================================================================

//
// Internal: declaration of a module interface (module gates and params).
// Example:
//    ModuleInterface(CLASSNAME)
//        Gate(NAME,TYPE)
//        Parameter(NAME,TYPES)
//    EndInterface
//
#define ModuleInterface(CLASSNAME)    static cModuleInterface::DeclarationItem CLASSNAME##__descr[] = {
#define Gate(NAME,TYPE)         {'G', #NAME, NULL,  TYPE},
#define Parameter(NAME,TYPES)   {'P', #NAME, TYPES, 0   },
#define EndInterface            {'E', NULL,  NULL,  0   }};

// internal: registers a module interface specified with the Interface..EndInterface macros
#define Register_ModuleInterface(CLASSNAME) \
  EXECUTE_ON_STARTUP(CLASSNAME##__if, modinterfaces.instance()->add(new cModuleInterface(#CLASSNAME, CLASSNAME##__descr));)

// internal: gate types. To be used with module interface declarations.
#define GateDir_Input      'I'
#define GateDir_Output     'O'

// internal: parameter allowed types. To be used with module interface declarations.
#define ParType_Const      "#"
#define ParType_Any        "*"
#define ParType_Numeric    "LDCXFTB"
#define ParType_Bool       "LDCXFTB"
#define ParType_String     "S"
#define ParType_XML        "M"

#endif
