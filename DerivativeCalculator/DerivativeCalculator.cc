/*
DerivativeCalculator.cc
Author: Neel Suresh

This program is used to calculate derivatives of polynomials and trigonometry functions. 
Run for further instructions on how to use the calculator.

How to run example:
g++ DerivativeCalculator.cc -o test
./test
*/

#define _USE_MATH_DEFINES
#include <iostream>
#include <sstream>
#include <string>
#include <math.h>
#include <cmath>
#include <regex>
using namespace std;

enum TrigType {
    COSINE,
    SINE
};

class AbstractTerm {
public:
    virtual AbstractTerm *derivative() = 0;
    virtual double evaluate(double d) = 0;
    virtual string toString() = 0;
    virtual int type() = 0;
};

class ConstantTerm : public AbstractTerm {
public:
    int a;

    ConstantTerm(int num) {
        a = num;
    }

    ConstantTerm() {
        a = 0;
    }

    AbstractTerm *derivative() {
        AbstractTerm *temp = new ConstantTerm(0);
        return temp;
    }

    double evaluate(double d) {
        return (double)(a + d - d);
    }

    string toString() {
        string s = to_string(abs(a));
        if (a > 0)
            return "+ " + s + " ";
        else if (a < 0)
            return "- " + s + " ";
        else
            return "+ 0 ";
    }

    int type() {
        return 2;
    }
};

class LinearTerm : public ConstantTerm {
public:
    LinearTerm(int num) {
        a = num;
    }

    LinearTerm() {
        a = 0;
    }

    AbstractTerm *derivative() {
        AbstractTerm *temp = new ConstantTerm(a);
        return temp;
    }

    double evaluate(double d) {
        return (double)(a * d);
    }

    string toString() {
        string s;
        s = to_string(abs(a)) + "x ";
        if (a > 0) {
            return "+ " + s;
        } else if (a < 0) {
            return "- " + s;
        }
        return "0 ";
    }

    int type() {
        return 1;
    }
};

class PolynomialTerm : public LinearTerm {
public:
    int b;

    PolynomialTerm(int num, int num2) {
        a = num;
        b = num2;
    }

    AbstractTerm *derivative() {
        AbstractTerm *temp;
        if (b > 2)
            temp = new PolynomialTerm(a * b, b - 1);
        else
            temp = new LinearTerm(2 * a);
        return temp;
    }

    double evaluate(double d) {
        return (double)(pow(d, b) * a);
    }

    string toString() {
        string s;
        s = to_string(abs(a)) + "x^" + to_string(b) + " ";
        if (a > 0) {
            return "+ " + s;
        }
        if (a < 0) {
            return "- " + s;
        }
        return "0 ";
    }

    int type() {
        return 0;
    }
};

class TrigTerm : public LinearTerm {
public:
    TrigType t;

    TrigTerm(int num, TrigType trig) {
        a = num;
        t = trig;
    }

    AbstractTerm *derivative() {
        AbstractTerm *temp;
        if (t == SINE)
            temp = new TrigTerm(a, COSINE);
        else
            temp = new TrigTerm(-1 * a, SINE);
        return temp;
    }

    double evaluate(double d) {
        double temp = d * M_PI / 180.0;
        if (t == SINE) {
            temp = sin(temp);
        }
        if (t == COSINE) {
            temp = cos(temp);
        }
        temp *= a;
        return temp;
    }

    string toString() {
        string s = to_string(abs(a));
        if (t == SINE)
            s += "sin(x) ";
        else
            s += "cos(x) ";
        if (a > 0) {
            return "+ " + s;
        }
        if (a < 0) {
            return "- " + s;
        }
        return "0 ";
    }

    int type() {
        if (t == SINE) {
            return 3;
        }
        return 4;
    }
};

template <class T>
class ProjNode {
public:
    T val;
    ProjNode<T> *next;

    ProjNode(T a, ProjNode<T> *b) {
        val = a;
        next = b;
    }

    ProjNode() {
        next = NULL;
    }
};

template <class T>
class ProjLinkedList {
public:
    ProjNode<T> *head;

    ProjLinkedList(ProjNode<T> *a) {
        head = a;
    }

    ProjLinkedList() {
        head = NULL;
    }

    void add(T term) {
        ProjNode<T> *temp = new ProjNode<T>();
        temp->val = term;
        temp->next = head;
        head = temp;
    }

    void printAll() {
        ProjNode<T> *temp = head;
        while (temp) {
            cout << temp->val.toString() << ", ";
            temp = temp->next;
        }
    }

    T getAt(int pos) {
        ProjNode<T> *current = head;
        for (int i = 0; i < pos; i++) {
            current = current->next;
        }
        return current->val;
    }

    int getSize() {
        ProjNode<T> *temp = head;
        int count = 0;
        while (temp->next != NULL) {
            count++;
            temp = temp->next;
        }
        return count;
    }
};

class Expression {
public:
    ProjLinkedList<AbstractTerm *> *link;

    Expression operator+=(AbstractTerm *rhs);

    Expression(ProjLinkedList<AbstractTerm *> *a) {
        link = a;
    }

    Expression() {
        link = new ProjLinkedList<AbstractTerm *>();
    }

    Expression *getDerivative() {
        ProjLinkedList<AbstractTerm *> *temp = link;
        ProjNode<AbstractTerm *> *t = temp->head;
        while (t->val->type() == 2) {
            t = t->next;
        }
        ProjNode<AbstractTerm *> *p = new ProjNode<AbstractTerm *>(t->val->derivative(), NULL);
        ProjLinkedList<AbstractTerm *> *newList = new ProjLinkedList<AbstractTerm *>(p);
        t = t->next;
        while (t != NULL) {
            if (t->val->type() != 2) {
                newList->add(t->val->derivative());
            }
            t = t->next;
        }
        Expression *e = new Expression(newList);
        return e;
    }

    double getEvaluation(double d) {
        double total = 0;
        ProjLinkedList<AbstractTerm *> *temp = link;
        while (temp->head != NULL) {
            total += temp->head->val->evaluate(d);
            temp->head = temp->head->next;
        }
        return total;
    }

    string toString() {
        ProjLinkedList<AbstractTerm *> *newList = new ProjLinkedList<AbstractTerm *>();
        ProjNode<AbstractTerm *> *tmp = link->head;
        while (tmp != NULL) {
            if (tmp->val->type() == 4)
                newList->add(tmp->val);
            tmp = tmp->next;
        }
        tmp = link->head;
        while (tmp != NULL) {
            if (tmp->val->type() == 3)
                newList->add(tmp->val);
            tmp = tmp->next;
        }
        tmp = link->head;
        while (tmp != NULL) {
            if (tmp->val->type() == 2)
                newList->add(tmp->val);
            tmp = tmp->next;
        }
        tmp = link->head;
        while (tmp != NULL) {
            if (tmp->val->type() == 1)
                newList->add(tmp->val);
            tmp = tmp->next;
        }
        tmp = link->head;
        while (tmp != NULL) {
            if (tmp->val->type() == 0)
                newList->add(tmp->val);
            tmp = tmp->next;
        }
        string ex = "";
        ProjNode<AbstractTerm *> *tmp2 = newList->head;
        while (tmp2 != NULL) {
            ex += tmp2->val->toString();
            tmp2 = tmp2->next;
        }
        return ex;
    }
};

Expression Expression::operator+=(AbstractTerm *rhs) {
    if (this->link->head == NULL) {
        this->link->head = new ProjNode<AbstractTerm *>(rhs, NULL);
        return *this;
    }
    ProjNode<AbstractTerm *> *temp = link->head;
    while (temp->next != NULL) {
        temp = temp->next;
    }
    temp->next = new ProjNode<AbstractTerm *>(rhs, NULL);
    return *this;
}

void promptUserForExpression(Expression &e) {
    cout << "Enter your expression term by term (type 'done' when finished):\n";
    cout << "Format for terms:\n";
    cout << "- Constant: c <value>\n";
    cout << "- Linear: l <coefficient>\n";
    cout << "- Polynomial: p <coefficient> <exponent>\n";
    cout << "- Trig: t <coefficient> <sin|cos>\n";

    string input;
    while (true) {
        cout << "> ";
        getline(cin, input);

        if (input == "done") {
            break;
        }

        stringstream ss(input);
        char type;
        ss >> type;

        if (type == 'c') {
            int value;
            ss >> value;
            e += new ConstantTerm(value);
        } else if (type == 'l') {
            int coefficient;
            ss >> coefficient;
            e += new LinearTerm(coefficient);
        } else if (type == 'p') {
            int coefficient, exponent;
            ss >> coefficient >> exponent;
            e += new PolynomialTerm(coefficient, exponent);
        } else if (type == 't') {
            int coefficient;
            string trigFunction;
            ss >> coefficient >> trigFunction;
            if (trigFunction == "sin") {
                e += new TrigTerm(coefficient, SINE);
            } else if (trigFunction == "cos") {
                e += new TrigTerm(coefficient, COSINE);
            } else {
                cout << "Invalid trig function. Please use 'sin' or 'cos'.\n";
            }
        } else {
            cout << "Invalid input. Please try again.\n";
        }
    }
}

int main() {
    Expression e;

    promptUserForExpression(e);

    cout << "Original Expression: " << e.toString() << "\n";
    cout << "Derivative: " << e.getDerivative()->toString() << "\n";

    return 0;
}