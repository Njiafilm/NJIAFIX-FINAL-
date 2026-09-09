from django.shortcuts import render, redirect
from django.http import JsonResponse
from .models import DeviceCategory, RepairGuide

def matengenezo_page(request):
    categories = DeviceCategory.objects.all()
    return render(request, 'matengenezo.html', {"categories": categories})


def api_categories(request):
    cats = DeviceCategory.objects.all()
    data = [{"id": c.id, "name": c.name, "icon": c.icon_name} for c in cats]
    return JsonResponse({"categories": data})


def api_guides(request):
    category_id = request.GET.get("category_id")
    query = request.GET.get("q", "").strip()

    guides = RepairGuide.objects.all()
    if category_id:
        guides = guides.filter(category_id=category_id)
    if query:
        guides = guides.filter(title__icontains=query) | guides.filter(symptom__icontains=query)

    data = [{
        "id": g.id,
        "title": g.title,
        "brand": g.brand,
        "symptom": g.symptom,
        "solution_steps": g.solution_steps,
        "difficulty": g.get_difficulty_display(),
        "image": g.image.url if g.image else None,
    } for g in guides[:100]]
    return JsonResponse({"guides": data})


def ongeza_repair_guide(request):
    """View ya kuchakata fomu ya kuongeza repair guide na kuihifadhi kwenye PostgreSQL"""
    if request.method == 'POST':
        category_id = request.POST.get('category')
        title = request.POST.get('title')
        brand = request.POST.get('brand')
        symptom = request.POST.get('symptom')
        solution_steps = request.POST.get('solution_steps')
        difficulty = request.POST.get('difficulty', 'easy')
        image = request.FILES.get('image')

        # Hakikisha taarifa muhimu haziko wazi kabla ya kuhifadhi
        if title and symptom and category_id:
            category_obj = DeviceCategory.objects.filter(id=category_id).first()
            
            if category_obj:
                RepairGuide.objects.create(
                    category=category_obj,
                    title=title,
                    brand=brand,
                    symptom=symptom,
                    solution_steps=solution_steps,
                    difficulty=difficulty,
                    image=image
                )
                # Ukishamaliza kuhifadhi, peleka mtumiaji kwenye ukurasa wa mafanikio au orodha
                return redirect('matengenezo_page')
        
    categories = DeviceCategory.objects.all()
    context = {
        'categories': categories,
        'difficulties': RepairGuide.DIFFICULTY
    }
    return render(request, 'ongeza_repair_guide.html', context)
